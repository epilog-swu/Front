package com.epi.epilog

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.*
import android.provider.Settings
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.epi.epilog.presentation.ApiResponse
import com.epi.epilog.presentation.FallDetectionActivity
import com.epi.epilog.presentation.theme.api.LocationData
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.epi.epilog.presentation.theme.api.SensorData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Timer
import java.util.TimerTask

class FallDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val sensorData = mutableListOf<SensorData>()
    private lateinit var retrofitService: RetrofitService
    private lateinit var locationManager: LocationManager
    private val channelId = "FallDetectionServiceChannel"
    private val notificationId = 1

    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var screenWakeLock: PowerManager.WakeLock
    private val timer = Timer()

    private var isModalShown = false
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech

    private var isTtsInitialized = false
    private var isFallDetected = false

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        initializeRetrofit()
        startForegroundService()
        acquireWakeLock()
        startDataTransmissionTimer()
        initializeLocationManager()
    }

    private fun startForegroundService() {
        createNotificationChannel()
        val notification = createNotification("Monitoring for falls in the background")
        startForeground(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Fall Detection Service", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fall Detection Service")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun initializeRetrofit() {
        val gson: Gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun getTokenFromSession(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FallDetectionService::WakeLock")
        wakeLock.acquire()
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun releaseScreenWakeLock() {
        if (screenWakeLock.isHeld) {
            screenWakeLock.release()
        }
    }

    private fun initializeLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            synchronized(sensorData) {
                sensorData.add(SensorData(x, y, z))
            }
        }
    }

    private fun startEmergencyProcedures() {
        mediaPlayer = MediaPlayer.create(this, R.raw.emergency_sound)
        mediaPlayer.start()

        Handler(Looper.getMainLooper()).postDelayed({
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()

            val fallQSound = MediaPlayer.create(applicationContext, R.raw.fall_q)
            fallQSound.start()

            fallQSound.setOnCompletionListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val fallASound = MediaPlayer.create(this, R.raw.fall_a)
                    fallASound.start()
                }, 3000)
            }

        }, 2000)
    }

    private fun sendEmergencyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없을 경우 빈 body 전송
            val locationData = LocationData(0.0, 0.0)  // 빈 LocationData 객체
            postLocationData(locationData)
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val lat = location.latitude
                val lon = location.longitude

                val locationData = LocationData(lat, lon)
                Log.d("좌표", "Latitude: $lat, Longitude: $lon")  // 현재 위도와 경도 값을 출력

                postLocationData(locationData)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        })
    }

    private fun postLocationData(locationData: LocationData) {
        val token = getTokenFromSession()

        if (!token.isNullOrEmpty()) {
            retrofitService.postLocationData(locationData, "Bearer $token").enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.success) {
                            Log.d("Location", "Location sent successfully")
                        } else {
                            Log.e("Location", "Failed to send location: ${response.errorBody()?.string()}")
                        }
                    } else {
                        Log.e("Location", "Failed to send location: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("Location", "Failed to send location: ${t.message}")
                }
            })
        }
    }

    private fun startDataTransmissionTimer() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                synchronized(sensorData) {
                    if (sensorData.isNotEmpty()) {
                        Log.d("SensorData", "Sending data: $sensorData")
                        postSensorData(ArrayList(sensorData))
                        sensorData.clear()
                    }
                }
            }
        }, 0, 1000)
    }

    private fun postSensorData(data: List<SensorData>) {
        val token = getTokenFromSession()
        if (token.isNullOrEmpty()) {
            Log.d("BloodSugarTimeInput", "Auth token is missing.")
            return
        }

        val call = retrofitService.postSensorData(data, "Bearer $token")
        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("SensorData", "Sensor Data Response: $responseBody")
                    updateNotification("Sensor Data Response: $responseBody")

                    if (responseBody == true) {
                        //해당 어플 접속하고 있지 않았을 때
                        startEmergencyProcedures() //사운드 부분
                        sendEmergencyLocation() //위치 전송 부분(에뮬레이터에서는 동작, 실제 워치에서 X)
                       //해당 어플 접속 시 (FallDetectionActivity로 이동)
//                     val intent = Intent(this@FallDetectionService, FallDetectionActivity::class.java)
//                     intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                       startActivity(intent)
                    }
                    // Wait a bit before posting sensor data again
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Continue posting sensor data
                        postSensorData(data)
                    }, 5000)  // Adjust delay as needed

                }  else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("BloodSugarTimeInput", "Response was not successful: Code ${response.code()}, Error Body: $errorBody")
                    updateNotification("Response was not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.e("BloodSugarTimeInput", "POST failed: ${t.message}")
                updateNotification("POST failed: ${t.message}")
            }
        })
    }

    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private val activityDestroyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isModalShown = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        releaseWakeLock()
        if (::screenWakeLock.isInitialized && screenWakeLock.isHeld) {
            screenWakeLock.release()
        }
        timer.cancel()
        unregisterReceiver(activityDestroyReceiver)
        speechRecognizer.destroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        if (::textToSpeech.isInitialized) {
            textToSpeech.shutdown()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
