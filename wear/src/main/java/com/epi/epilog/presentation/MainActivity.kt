package com.epi.epilog.presentation

import CalendarInitializer
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.epi.epilog.databinding.ActivityMainBinding
import com.epi.epilog.presentation.theme.Data
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.epi.epilog.presentation.theme.api.SensorData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.kizitonwose.calendar.view.WeekCalendarView
import java.time.LocalDate

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chart: LineChart

    private val bloodSugarChartData = ArrayList<Entry>() // 데이터 배열
    private lateinit var lineData: LineData

    private var todayDataCounts: Int = 0
    private lateinit var retrofitService: RetrofitService

    private lateinit var weekCalendarView: WeekCalendarView
    private lateinit var calendarInitializer: CalendarInitializer // 변경된 부분: 변수 선언

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val sensorData = mutableListOf<SensorData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chart = binding.graphBloodSugarChart
        weekCalendarView = binding.calendarView

        // CalendarInitializer 사용
        calendarInitializer = CalendarInitializer(this, weekCalendarView, this::onDateSelected) // 변경된 부분: 초기화
        calendarInitializer.initWeekCalendarView()

        //로그인 시 토큰 발급
        initializeRetrofit()
        postData()

        // ChartInitializer 사용
        ChartInitializer(chart).initChart()

        // 버튼 클릭 리스너 설정
        setButtonListeners()

        // Initialize sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
    }

    private fun setButtonListeners() {
        binding.btnBloodSugarRecord.setOnClickListener {
            navigateToActivity(BloodSugarActivity::class.java)
        }

        binding.btnCheckMedicine.setOnClickListener {
            navigateToActivity(MedicineActivity::class.java)
        }

        binding.btnCheckMeals.setOnClickListener {
            navigateToActivity(MealActivity::class.java)
        }
    }

    private fun getTokenFromSession(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    private fun disableButtons() {
        binding.btnBloodSugarRecord.isEnabled = false
        binding.btnCheckMedicine.isEnabled = false
        binding.btnCheckMeals.isEnabled = false
    }

    private fun enableButtons() {
        binding.btnBloodSugarRecord.isEnabled = true
        binding.btnCheckMedicine.isEnabled = true
        binding.btnCheckMeals.isEnabled = true
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass).apply {
            calendarInitializer.getSelectedDate()?.let {
                putExtra("SELECTED_DATE", it.toString())
            }
        }
        startActivity(intent)
    }

    private fun onDateSelected(date: LocalDate) {
    }

    private fun initializeRetrofit() {
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun postData() {
        val post = Data()
        val call = retrofitService.postData(post)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d("BloodSugarTimeInput", "Server Response: $it")
                        saveTokenToSession(it)
                        runOnUiThread {
                            setupButtonListeners()
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("BloodSugarTimeInput", "Error Response: $errorBody")
                    Log.d("BloodSugarTimeInput", "Response Code: ${response.code()}")
                    runOnUiThread {
                        disableButtons()
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("BloodSugarTimeInput", "POST failed: ${t.message}")
                runOnUiThread {
                    disableButtons()
                }
            }
        })
    }

    private fun setupButtonListeners() {
        val token = getTokenFromSession()
        if (token.isNullOrEmpty()) {
            disableButtons()
        } else {
            enableButtons()
        }
    }

    private fun saveTokenToSession(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("AuthToken", token).apply()
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
        val token = getTokenFromSession()
        if (token.isNullOrEmpty()) {
            Log.d("BloodSugarTimeInput", "Auth token is missing.")
            return
        }

        val call = retrofitService.postSensorData(sensorData, "Bearer $token")
        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("BloodSugarTimeInput", "Sensor Data Response: $responseBody")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("BloodSugarTimeInput", "Response was not successful: Code ${response.code()}, Error Body: $errorBody")
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.e("BloodSugarTimeInput", "POST failed: ${t.message}")
            }
        })
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 필요한 경우 정확도 변경 처리
    }
}
