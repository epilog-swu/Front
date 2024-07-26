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
import com.epi.epilog.presentation.theme.api.SensorDataWithRotation
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.net.URISyntaxException
import java.util.Timer
import java.util.TimerTask

class FallDetectionService : Service(), SensorEventListener, LocationListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private val sensorData = mutableListOf<SensorDataWithRotation>()
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
    private var webSocketClient: WebSocketClient? = null

    private var lastTimestamp: Long = 0
    private var rotationAngles = FloatArray(3)

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)

        initializeRetrofit()
        startForegroundService()
        acquireWakeLock()
        startDataTransmissionTimer()
        initializeLocationManager()
        initializeWebSocket()
    }

    private var reconnectTimer: Timer? = null
    private val reconnectDelay = 5000L  // 재연결 시도 간격 (밀리초 단위)

    // 웹소켓 초기화
    private fun initializeWebSocket() {
        val token = getTokenFromSession()
        try {
            val serverEndpoint = "epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com"
            val uri = URI("ws://$serverEndpoint/detection/fall?token=$token")
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d("WebSocket", "Opened")
                    stopReconnectTimer()
                }

                override fun onMessage(message: String?) {
                    Log.d("WebSocket", "Message received: $message")
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d("WebSocket", "Closed: $reason")
                    startReconnectTimer()
                }

                override fun onError(ex: Exception?) {
                    Log.e("WebSocket", "Error: ${ex?.message}")
                    startReconnectTimer()
                }
            }
            webSocketClient?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun startReconnectTimer() {
        if (reconnectTimer == null) {
            reconnectTimer = Timer()
            reconnectTimer?.schedule(object : TimerTask() {
                override fun run() {
                    initializeWebSocket()
                }
            }, reconnectDelay, reconnectDelay)
        }
    }

    private fun stopReconnectTimer() {
        reconnectTimer?.cancel()
        reconnectTimer = null
    }

    private fun sendWebSocketEmergencyLocation(locationData: LocationData, isPermissionDenied: Boolean) {
        if (webSocketClient != null && webSocketClient!!.isOpen) {
            val message = Gson().toJson(mapOf(
                "event" to "emer",
                "data" to mapOf(
                    "latitude" to locationData.latitude,
                    "longitude" to locationData.longitude
                )
            ))
            webSocketClient?.send(message)
            Log.d("WebSocket", "Sent emergency location: $message")
        } else {
            Log.e("WebSocket", "WebSocket is not connected")
        }
    }

    private fun sendWebSocketSensorData(data: List<SensorDataWithRotation>) {
        if (webSocketClient != null && webSocketClient!!.isOpen) {
            val message = Gson().toJson(mapOf(
                "event" to "fall",
                "data" to mapOf(
                    "fall" to data.map { mapOf(
                        "x" to it.x,
                        "y" to it.y,
                        "z" to it.z,
                        "rotationX" to it.rotationX,
                        "rotationY" to it.rotationY,
                        "rotationZ" to it.rotationZ
                    ) }
                )
            ))
            webSocketClient?.send(message)
            Log.d("WebSocket", "Sent fall detection data: $message")
        } else {
            Log.e("WebSocket", "WebSocket is not connected")
        }
    }

    private fun startForegroundService() {
        createNotificationChannel()
        val notification = createNotification("Monitoring for falls in the background")
        startForeground(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Fall Detection Service", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FallDetectionService::WakeLock")
        wakeLock.acquire()
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationManager() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
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
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    synchronized(sensorData) {
                        if (!isDataTransmissionPaused) {
                            sensorData.add(SensorDataWithRotation(x, y, z, rotationAngles[0], rotationAngles[1], rotationAngles[2]))
                        }
                    }
                }
                Sensor.TYPE_GYROSCOPE -> {
                    if (lastTimestamp != 0L) {
                        val dt = (it.timestamp - lastTimestamp) * NS2S
                        rotationAngles[0] += it.values[0] * dt
                        rotationAngles[1] += it.values[1] * dt
                        rotationAngles[2] += it.values[2] * dt
                    }
                    lastTimestamp = it.timestamp
                }
            }
        }
    }

    companion object {
        private const val NS2S = 1.0f / 1000000000.0f
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
            sendWebSocketEmergencyLocation(LocationData(0.0, 0.0), true)
            return
        }

        val lat = currentLocation?.latitude ?: 0.0
        val lon = currentLocation?.longitude ?: 0.0
        val locationData = LocationData(lat, lon)
        Log.d("좌표", "Latitude: $lat, Longitude: $lon")

        sendWebSocketEmergencyLocation(locationData, false)
    }

    private fun startDataTransmissionTimer() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                synchronized(sensorData) {
                    if (sensorData.isNotEmpty()) {
                        Log.d("SensorData", "Sending data: $sensorData")
                        sendWebSocketSensorData(ArrayList(sensorData))
                        sensorData.clear()
                    }
                }
            }
        }, 0, 1000)
    }

    private fun updateNotification(isFallDetected: Boolean) {
        try {
            val contentText = if (isFallDetected) "낙상 상황입니다" else "낙상 상황이 아닙니다"
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Dialog")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.logo)
                .build()

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
        webSocketClient?.close()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
