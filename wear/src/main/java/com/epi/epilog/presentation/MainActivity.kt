package com.epi.epilog.presentation

import CalendarInitializer
import ChartInitializer
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

class MainActivity : ComponentActivity() {

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


        // ChartInitializer 사용
        ChartInitializer(chart).initChart()

        // 버튼 클릭 리스너 설정
        setButtonListeners()

    }

    // 버튼 클릭 리스너
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

    //버튼 클릭 리스너에 포함된 메서드 - 인텐트 적용(날짜 보내기)
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass).apply {
            calendarInitializer.getSelectedDate()?.let {
                putExtra("SELECTED_DATE", it.toString())
            }
        }
        startActivity(intent)
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


    private fun onDateSelected(date: LocalDate) {
    }

}
