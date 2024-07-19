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
    private var webSocketClient: WebSocketClient? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)

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
            // URI 객체를 사용하여 웹소켓 서버에 연결
            val serverEndpoint = "epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com"

// Construct the WebSocket URI with the server endpoint and token
            val uri = URI("ws://$serverEndpoint/detection/fall?token=$token")
            webSocketClient = object : WebSocketClient(uri) {
                // 웹소켓 연결이 열렸을 때 호출
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d("WebSocket", "Opened")
                    stopReconnectTimer()
                }

                // 서버로부터 메시지를 받을 때 호출
                override fun onMessage(message: String?) {
                    Log.d("WebSocket", "Message received: $message")
                }

                // 웹소켓 연결이 닫혔을 때 호출
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d("WebSocket", "Closed: $reason")
                    startReconnectTimer()
                }

                // 웹소켓 오류가 발생했을 때 호출
                override fun onError(ex: Exception?) {
                    Log.e("WebSocket", "Error: ${ex?.message}")
                    startReconnectTimer()
                }
            }
            // 웹소켓 연결 시작
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

    // 웹소켓을 통해 비상 위치 전송
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

    // 웹소켓을 통해 센서 데이터 전송
    private fun sendWebSocketSensorData(data: List<SensorData>) {
        if (webSocketClient != null && webSocketClient!!.isOpen) {
            val message = Gson().toJson(mapOf(
                "event" to "fall",
                "data" to mapOf(
                    "fall" to data.map { mapOf("x" to it.x, "y" to it.y, "z" to it.z) }
                )
            ))
            webSocketClient?.send(message)
            Log.d("WebSocket", "Sent fall detection data: $message")
        } else {
            Log.e("WebSocket", "WebSocket is not connected")
        }
    }

    // 포그라운드 서비스 시작
    private fun startForegroundService() {
        createNotificationChannel()
        val notification = createNotification("Monitoring for falls in the background")
        startForeground(notificationId, notification)
    }

    // 알림 채널 생성
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Fall Detection Service", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // 알림 생성
    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fall Detection Service")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    // Retrofit 초기화
    private fun initializeRetrofit() {
        val gson: Gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    // 세션에서 토큰 가져오기
    private fun getTokenFromSession(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    // WakeLock 획득
    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FallDetectionService::WakeLock")
        wakeLock.acquire()
    }

    // 위치 관리자 초기화
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

    // 위치 변경 시 호출
    override fun onLocationChanged(location: Location) {
        currentLocation = location
        Log.d("LocationUpdate", "New Location: Latitude ${location.latitude}, Longitude ${location.longitude}")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    // 센서 데이터 변경 시 호출
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

    // 비상 절차 시작
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
                        // 모든 소리가 끝난 후 데이터 전송 재개
                        isDataTransmissionPaused = false
                    }
                }, 3000)
            }

        }, 3500)
    }

    // 비상 위치 전송
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



    // 데이터 전송 타이머 시작
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


    // 알림 업데이트
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
