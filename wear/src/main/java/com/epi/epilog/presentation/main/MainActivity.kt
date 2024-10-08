package com.epi.epilog.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.epi.epilog.R
import com.epi.epilog.presentation.blood.BloodSugarActivity
import com.epi.epilog.presentation.blood.BloodSugarDatas
import com.epi.epilog.databinding.ActivityMainBinding
import com.epi.epilog.presentation.fall.FallDetectionService
import com.epi.epilog.presentation.meal.MealActivity
import com.epi.epilog.presentation.medicine.MedicineActivity
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.github.mikephil.charting.charts.LineChart
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

    private lateinit var retrofitService: RetrofitService

    private lateinit var weekCalendarView: WeekCalendarView
    private lateinit var calendarInitializer: CalendarInitializer // 변경된 부분: 변수 선언
    private lateinit var chartInitializer: ChartInitializer // 변경된 부분: 변수 선언


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Retrofit 초기화
        initializeRetrofit()

        chart = binding.graphBloodSugarChart
        weekCalendarView = binding.calendarView

        // ChartInitializer 사용
        chartInitializer = ChartInitializer(chart)
        chartInitializer.initChart()

        // calendar.CalendarInitializer 사용
        calendarInitializer = CalendarInitializer(this, weekCalendarView, this::onDateSelected)
        calendarInitializer.initWeekCalendarView()

        // 오늘 날짜 선택 및 차트 초기화
        val today = LocalDate.now()
        onDateSelected(today)
        calendarInitializer.selectDate(today)


        // 버튼 클릭 리스너 설정
        setButtonListeners()

        // FallDetectionService 실행
        startFallDetectionService()

        // Intent로 전달된 날짜가 있는지 확인하고, 있으면 해당 날짜로 차트를 업데이트
        val selectedDate = intent.getStringExtra("SELECTED_DATE")?.let {
            LocalDate.parse(it)
        }
        selectedDate?.let {
            onDateSelected(it)
            calendarInitializer.selectDate(it)
        }


    }



    // Retrofit 초기화 메서드
    private fun initializeRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
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

    private fun startFallDetectionService() {
        val serviceIntent = Intent(this, FallDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun onDateSelected(date: LocalDate) {
        chartInitializer.updateChart(date) // 날짜 선택 시 ChartInitializer를 통해 차트 업데이트
        updateBloodSugarRecordCount(date) // 날짜 선택 시 혈당 기록 개수 업데이트
    }

    private fun updateBloodSugarRecordCount(date: LocalDate) {
        val token = getTokenFromSession()
        if (token.isNullOrEmpty()) {
            Log.d("MainActivity", "Auth token is missing.")
            return
        }

        val call = retrofitService.getBloodSugarDatas(date.toString(), "Bearer $token")
        call.enqueue(object : Callback<BloodSugarDatas> {
            override fun onResponse(call: Call<BloodSugarDatas>, response: Response<BloodSugarDatas>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val recordCount = it.diabetes.size
                        updateRecordCountTextView(recordCount)
                    } ?: run {
                        updateRecordCountTextView(0)
                    }
                } else {
                    updateRecordCountTextView(0)
                }
            }

            override fun onFailure(call: Call<BloodSugarDatas>, t: Throwable) {
                Log.e("MainActivity", "Network error: ${t.message}", t)
                updateRecordCountTextView(0)
            }
        })
    }

    private fun getTokenFromSession(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    private fun updateRecordCountTextView(count: Int) {
        val textView = findViewById<TextView>(R.id.blood_sugar_records_counts)
        textView.text = "오늘의 혈당 기록: $count"
    }
}