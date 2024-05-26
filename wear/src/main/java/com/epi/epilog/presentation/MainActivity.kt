package com.epi.epilog.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.epi.epilog.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.LineChart
import com.kizitonwose.calendar.view.WeekCalendarView
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chart: LineChart
    private lateinit var weekCalendarView: WeekCalendarView
    private lateinit var calendarInitializer: CalendarInitializer // 변경된 부분: 변수 선언


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

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass).apply {
            calendarInitializer.getSelectedDate()?.let {
                putExtra("SELECTED_DATE", it.toString()) // 선택된 날짜 정보를 인텐트에 추가
            }
        }
        startActivity(intent)
    }

    private fun onDateSelected(date: LocalDate) {

    }
}
