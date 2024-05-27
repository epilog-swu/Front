package com.epi.epilog.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
<<<<<<< HEAD
import android.view.ViewTreeObserver
=======
>>>>>>> origin/main
import androidx.activity.ComponentActivity
import com.epi.epilog.databinding.ActivityMainBinding
import com.epi.epilog.presentation.theme.Data
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.github.mikephil.charting.charts.LineChart
<<<<<<< HEAD
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

=======
import com.kizitonwose.calendar.view.WeekCalendarView
import java.time.LocalDate
>>>>>>> origin/main

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chart: LineChart
<<<<<<< HEAD

    private val bloodSugarChartData = ArrayList<Entry>() // 데이터 배열
    private lateinit var lineData: LineData

    private var todayDataCounts: Int = 0
    private lateinit var retrofitService: RetrofitService
=======
    private lateinit var weekCalendarView: WeekCalendarView
    private lateinit var calendarInitializer: CalendarInitializer // 변경된 부분: 변수 선언
>>>>>>> origin/main


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

<<<<<<< HEAD
        //로그인 시 토큰 발급
        initializeRetrofit()
        postData()
        
        //버튼 클릭 리스너 구현
=======
        // ChartInitializer 사용
        ChartInitializer(chart).initChart()

        // 버튼 클릭 리스너 설정
        setButtonListeners()
    }

    private fun setButtonListeners() {
>>>>>>> origin/main
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

<<<<<<< HEAD
    //토큰 가져오기 메서드
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
        binding.btnBloodSugarRecord.setOnClickListener {
            startActivity(Intent(this, BloodSugarActivity::class.java))
        }
        binding.btnCheckMedicine.setOnClickListener {
            startActivity(Intent(this, MedicineActivity::class.java))
        }
        binding.btnCheckMeals.setOnClickListener {
            startActivity(Intent(this, MealActivity::class.java))
        }
    }
    // 차트 데이터 초기화 메서드
    private fun initChartData() {
        // 일단, 더미데이터
        //x는 그냥 길이, y는 실제 수치라고 생각하면 됩니다.
        bloodSugarChartData.add(Entry(0f, 50f))
        bloodSugarChartData.add(Entry(240f, 70f))
        bloodSugarChartData.add(Entry(480f, 120f))
        bloodSugarChartData.add(Entry(720f, 90f))
        bloodSugarChartData.add(Entry(960f, 250f))
        bloodSugarChartData.add(Entry(1200f, 300f))
        bloodSugarChartData.add(Entry(1440f, 30f))

        todayDataCounts = 3

        //데이터 이름 및 Line 설정
        val set = LineDataSet(bloodSugarChartData, "Blood Sugar Levels")
        set.setDrawValues(false)

        //색상
        set.color = Color.parseColor("#FFFFFF")// Line의 색상을 설정 //흰색
        val circleColor = Color.parseColor("#8377D8") //메인보라
        set.setCircleColors(circleColor) //데이터 셋의 색상을 설정 //메인 보라

        //강조 표시
        set.highLightColor = Color.TRANSPARENT
        set.mode = LineDataSet.Mode.LINEAR

        lineData = LineData(set)
=======
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass).apply {
            calendarInitializer.getSelectedDate()?.let {
                putExtra("SELECTED_DATE", it.toString()) // 선택된 날짜 정보를 인텐트에 추가
            }
        }
        startActivity(intent)
>>>>>>> origin/main
    }

    private fun onDateSelected(date: LocalDate) {

    }
<<<<<<< HEAD
    private fun initializeRetrofit() {
        val gson : Gson = GsonBuilder()
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
                        saveTokenToSession(it)  // Save the token
                        runOnUiThread {
                            setupButtonListeners()  // Update button states after token is saved
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("BloodSugarTimeInput", "Error Response: $errorBody")
                    Log.d("BloodSugarTimeInput", "Response Code: ${response.code()}")
                    runOnUiThread {
                        disableButtons()  // Optionally disable buttons if token fetch fails
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("BloodSugarTimeInput", "POST failed: ${t.message}")
                runOnUiThread {
                    disableButtons()  // Disable buttons on failure
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

}
=======
}
>>>>>>> origin/main
