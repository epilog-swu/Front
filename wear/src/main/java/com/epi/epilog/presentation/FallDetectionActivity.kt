package com.epi.epilog.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.epi.epilog.presentation.theme.api.SensorData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FallDetectionActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val sensorData = mutableListOf<SensorData>()
    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)

        // Initialize Retrofit
        initializeRetrofit()
    }

    private fun initializeRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            sensorData.add(SensorData(x, y, z))

            if (sensorData.size == 100) {
                Log.d("SensorData", "Sending data: $sensorData")
                postSensorData()
                sensorData.clear()
            }
        }
    }

    private fun postSensorData() {

//        val token="eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6MSwiaWF0IjoxNzE2OTY0MzY1LCJleHAiOjE4MDMzNjQzNjV9.B_02rRCymUCDjNSDEexLKfvTA3qkpZ1PEdZ9z2VFiNY"
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("AuthToken", "")
        if (token.isNullOrEmpty()) {
            Log.d("BloodSugarTimeInput", "Auth token is missing.")
            return
        }

        val call = retrofitService.postSensorData(sensorData, "Bearer $token")
        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {

                    val responseBody = response.body()
                    Log.d("BloodSugarTimeInput", "Sensor Data Response: $responseBody")            }}

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.d("BloodSugarTimeInput", "POST failed: ${t.message}")
            }
        })
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 필요한 경우 정확도 변경 처리
    }
}
