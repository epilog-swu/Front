package com.epi.epilog.presentation.fall

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.epi.epilog.R
import com.epi.epilog.presentation.theme.api.LocationData
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.epi.epilog.presentation.theme.api.SensorData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.exceptions.WebsocketNotConnectedException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.net.URISyntaxException
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class FallDetectionService : Service(), SensorEventListener, LocationListener {

    private val medicineChannelId = "MedicineActivityChannel" // 알림 채널 ID

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private val sensorData = mutableListOf<SensorData>()
    private lateinit var retrofitService: RetrofitService
    private lateinit var locationManager: LocationManager
    private val channelId = "FallDetectionServiceChannel"
    private val notificationId = 1

    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var scheduledExecutorService: ScheduledExecutorService

    private var isEmergencyTriggered = false
    private var currentLocation: Location? = null
    private lateinit var mediaPlayer: MediaPlayer

    private var isDataTransmissionPaused = false
    private var webSocketClient: WebSocketClient? = null

    private val fallDetectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isFallConfirmed = intent?.getBooleanExtra("isFallConfirmed", false) ?: false
            if (isFallConfirmed) {
                sendWebSocketEmergencyLocation(LocationData(currentLocation?.latitude ?: 0.0, currentLocation?.longitude ?: 0.0), false)
            }
            isDataTransmissionPaused = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // 측정 주파수를 80Hz로 설정 (기존 100Hz보다 줄임)
        val sensorDelay = (1000000 / 100).toInt() // 마이크로초 단위로 계산

        sensorManager.registerListener(this, accelerometer, sensorDelay)
        sensorManager.registerListener(this, gyroscope, sensorDelay)

        initializeRetrofit()
        startForegroundService()
        acquireWakeLock()
        initializeLocationManager()
        initializeWebSocket()

        // FallDetectionActivity의 결과를 수신할 BroadcastReceiver 등록
        val filter = IntentFilter("com.epi.epilog.FALL_DETECTION_RESULT")
        registerReceiver(fallDetectionReceiver, filter)

        // ScheduledExecutorService 초기화 및 데이터 전송 타이머 시작
        scheduledExecutorService = Executors.newScheduledThreadPool(1)
        startDataTransmissionScheduler()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(fallDetectionReceiver)
        sensorManager.unregisterListener(this)
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        if (::wakeLock.isInitialized) {
            wakeLock.release()
        }
        webSocketClient?.close()

        // ScheduledExecutorService 종료
        scheduledExecutorService.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
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
                    val jsonObject = Gson().fromJson(message, JsonObject::class.java)
                    val success = jsonObject.get("success").asBoolean
                    val event = jsonObject.get("event").asString
                    if (event == "com/epi/epilog/presentation/fall" && success) {
                        isDataTransmissionPaused = true
                        val intent = Intent(this@FallDetectionService, FallDetectionActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
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
            val gson = Gson()
            val chunkedData = data.chunkedBySize(100)  // 데이터를 100개씩 청크로 나눔
            val totalChunks = chunkedData.size

            for ((index, chunk) in chunkedData.withIndex()) {
                val jsonString = gson.toJson(mapOf(
                    "event" to "com/epi/epilog/presentation/fall",
                    "chunkIndex" to index,
                    "totalChunks" to totalChunks,
                    "data" to mapOf(
                        "com/epi/epilog/presentation/fall" to chunk.map { mapOf(
                            "accX" to roundToTwoDecimalPlaces(it.accX),
                            "accY" to roundToTwoDecimalPlaces(it.accY),
                            "accZ" to roundToTwoDecimalPlaces(it.accZ),
                            "gyroX" to roundToTwoDecimalPlaces(it.gyroX),
                            "gyroY" to roundToTwoDecimalPlaces(it.gyroY),
                            "gyroZ" to roundToTwoDecimalPlaces(it.gyroZ)
                        )
                        }
                    )
                ))

                try {
                    webSocketClient?.send(jsonString)
//                    Log.d("WebSocket", jsonString)
                    Log.d("WebSocket", "Sent fall detection data chunk $index of $totalChunks")
                } catch (e: WebsocketNotConnectedException) {
                    Log.e("WebSocket", "WebSocket is not connected", e)
                }
            }
        } else {
            Log.e("WebSocket", "WebSocket is not connected")
        }
    }

    private fun List<SensorData>.chunkedBySize(chunkSize: Int): List<List<SensorData>> {
        val chunks = mutableListOf<List<SensorData>>()
        var start = 0
        while (start < this.size) {
            val end = (start + chunkSize).coerceAtMost(this.size)
            chunks.add(this.subList(start, end))
            start += chunkSize
        }
        return chunks
    }

    private fun roundToTwoDecimalPlaces(value: Float): Float {
        return String.format("%.2f", value).toFloat()
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
            val sensorType = it.sensor.type

            synchronized(sensorData) {
                if (!isDataTransmissionPaused) {
                    when (sensorType) {
                        Sensor.TYPE_ACCELEROMETER -> sensorData.add(SensorData(x, y, z, 0f, 0f, 0f))
                        Sensor.TYPE_GYROSCOPE -> sensorData.add(SensorData(0f, 0f, 0f, x, y, z))
                    }
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

    // 데이터 전송 스케줄러 시작
    private fun startDataTransmissionScheduler() {
        scheduledExecutorService.scheduleAtFixedRate({
            try {
                synchronized(sensorData) {
                    if (sensorData.isNotEmpty()) {
                        Log.d("SensorData", "Sending data: $sensorData")
                        sendWebSocketSensorData(ArrayList(sensorData))
                        sensorData.clear()
                    }
                }
            } catch (e: Exception) {
                Log.e("SchedulerTask", "Error in SchedulerTask", e)
            }
        }, 0, 1, TimeUnit.SECONDS)
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
}