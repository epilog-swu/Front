package com.epi.epilog.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

class FallDetectionActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val sensorData = mutableListOf<Triple<Float, Float, Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // 100Hz 데이터 수집을 위해 10ms 간격 사용
        sensorManager.registerListener(this, accelerometer, 10000) // 10ms 간격
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            sensorData.add(Triple(x, y, z))

            // 1초마다 로그 출력 후 배열 초기화
            if (sensorData.size == 100) {
                logSensorData()
                sensorData.clear()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 필요한 경우 정확도 변경 처리
    }

    private fun logSensorData() {
        Log.d("SensorData", "Data: $sensorData")
    }
}
