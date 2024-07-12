package com.epi.epilog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.epi.epilog.presentation.ApiResponse
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

class FallDetectionService : Service(), SensorEventListener, LocationListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val sensorData = mutableListOf<SensorData>()
    private lateinit var retrofitService: RetrofitService
    private lateinit var locationManager: LocationManager
    private val channelId = "FallDetectionServiceChannel"
    private val notificationId = 1

    private lateinit var wakeLock: PowerManager.WakeLock
    private val timer = Timer()

    private var isEmergencyTriggered = false
    private var currentLocation: Location? = null
    private lateinit var mediaPlayer: MediaPlayer

    private var isDataTransmissionPaused = false

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

    @SuppressLint("MissingPermission")
    private fun initializeLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Location", "Location permission not granted")
            return
        }
        val providers = locationManager.allProviders
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, this)
            Log.d("Location", "GPS Provider requested")
        }
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10f, this)
            Log.d("Location", "Network Provider requested")
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        Log.d("LocationUpdate", "New Location: Latitude ${location.latitude}, Longitude ${location.longitude}")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            synchronized(sensorData) {
                if (!isDataTransmissionPaused) {
                    sensorData.add(SensorData(x, y, z))
                }
            }
        }
    }

    private fun startEmergencyProcedures() {
        mediaPlayer = MediaPlayer.create(this, R.raw.emergency_sound)
        mediaPlayer.start()

        Handler(Looper.getMainLooper()).postDelayed({
            mediaPlayer.stop()
            if (mediaPlayer.isPlaying) {
                mediaPlayer.release()
            }

            val fallQSound = MediaPlayer.create(applicationContext, R.raw.fall_q)
            fallQSound.start()

            fallQSound.setOnCompletionListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    val fallASound = MediaPlayer.create(this, R.raw.fall_a)
                    fallASound.start()

                    fallASound.setOnCompletionListener {
                        // Resume data transmission after all sounds are done
                        isDataTransmissionPaused = false
                    }
                }, 3000)
            }

        }, 3500)
    }

    private fun sendEmergencyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Location", "Permission not granted, sending empty body")
            postLocationData(LocationData(0.0, 0.0), true)
            return
        }

        val lat = currentLocation?.latitude ?: 0.0
        val lon = currentLocation?.longitude ?: 0.0
        val locationData = LocationData(lat, lon)
        Log.d("좌표", "Latitude: $lat, Longitude: $lon")

        postLocationData(locationData, false)
    }

    private fun postLocationData(locationData: LocationData, isPermissionDenied: Boolean) {
        val token = getTokenFromSession()

        if (!token.isNullOrEmpty()) {
            retrofitService.postLocationData(locationData, "Bearer $token").enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.success) {
                            if (isPermissionDenied) {
                                Log.d("Location", "Location permission denied, sent empty body successfully.")
                            } else {
                                Log.d("LocationData", "Location sent successfully: ${apiResponse.success} Latitude: ${locationData.latitude}, Longitude: ${locationData.longitude}")
                            }
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
            Log.d("SensorData", "Auth token is missing.")
            return
        }

        val call = retrofitService.postSensorData(data, "Bearer $token")
        call.enqueue(object : Callback<Boolean> {

            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("SensorData", "Sensor Data Response: $responseBody")
                    updateNotification(responseBody == true)

                    if (responseBody == true && !isEmergencyTriggered) {
                        isEmergencyTriggered = true
                        isDataTransmissionPaused = true // Pause data transmission
                        startEmergencyProcedures()
                        sendEmergencyLocation()

                        // 15초 후에 낙상 감지 재시작
                        Handler(Looper.getMainLooper()).postDelayed({
                            isEmergencyTriggered = false
                        }, 15000)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SensorData", "Response was not successful: Code ${response.code()}, Error Body: $errorBody")
                    updateNotification(false)
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.e("SensorData", "POST failed: ${t.message}")
                updateNotification(false)
            }
        })
    }

    private fun updateNotification(isFallDetected: Boolean) {
        try {
            val contentText = if (isFallDetected) "낙상 상황입니다" else "낙상 상황이 아닙니다"
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Dialog")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.logo)
                .build()

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
            Log.d("Notification", "Notification posted successfully: $contentText")
        } catch (e: Exception) {
            Log.e("Notification", "Failed to post notification", e)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        if (::wakeLock.isInitialized) {
            wakeLock.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
