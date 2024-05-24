package com.epi.epilog.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.epi.epilog.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.WeekDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.epi.epilog.R
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chart: LineChart
    private lateinit var weekCalendarView: WeekCalendarView

    private val bloodSugarChartData = ArrayList<Entry>() // 데이터 배열
    private lateinit var lineData: LineData

    private var todayDataCounts: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // chart 초기화
        chart = binding.graphBloodSugarChart

        // 주단위 캘린더 초기화
        weekCalendarView = binding.calendarView

        weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView: TextView = view.findViewById(R.id.calendarDayText)
        }

        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        val startDate = currentMonth.minusMonths(100).atStartOfMonth() // Adjust as needed
        val endDate = currentMonth.plusMonths(100).atEndOfMonth()  // Adjust as needed
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library
        weekCalendarView.setup(startDate, endDate, firstDayOfWeek)
        weekCalendarView.scrollToWeek(currentDate)


        // 그래프 데이터 설정 및 차트 초기화
        initChartData()
        initChart()

        // 버튼 클릭 리스너 구현
        binding.btnBloodSugarRecord.setOnClickListener {
            val intent = Intent(this, BloodSugarActivity::class.java)
            startActivity(intent)
        }

        binding.btnCheckMedicine.setOnClickListener {
            val intent = Intent(this, MedicineActivity::class.java)
            startActivity(intent)
        }

        binding.btnCheckMeals.setOnClickListener {
            val intent = Intent(this, MealActivity::class.java)
            startActivity(intent)
        }

        binding.btnCheckGraph.setOnClickListener {
            val intent = Intent(this, GraphActivity::class.java)
            startActivity(intent)
        }
    }

    // 주단위 캘린더 초기화 메서드
    // 주단위 캘린더 초기화 메서드
    // 주단위 캘린더 초기화 메서드
//    private fun initWeekCalendarView() {
//        if (::weekCalendarView.isInitialized) {
//            weekCalendarView.setup(
//                startDate = LocalDate.now().minusMonths(1),
//                endDate = LocalDate.now().plusMonths(1),
//                firstDayOfWeek = DayOfWeek.MONDAY
//            )
//
//            weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
//                override fun create(view: View) = DayViewContainer(view)
//                override fun bind(container: DayViewContainer, data: WeekDay) {
//                    container.textView.text = data.date.dayOfMonth.toString()
//                }
//            }
//
//            weekCalendarView.weekScrollListener = { weekDays ->
//                // Handle week scroll event
//            }
//        }
//    }


    // 차트 데이터 초기화 메서드
    private fun initChartData() {
        // 일단, 더미데이터
        bloodSugarChartData.add(Entry(0f, 50f))
        bloodSugarChartData.add(Entry(240f, 70f))
        bloodSugarChartData.add(Entry(480f, 120f))
        bloodSugarChartData.add(Entry(720f, 90f))
        bloodSugarChartData.add(Entry(960f, 250f))
        bloodSugarChartData.add(Entry(1200f, 300f))
        bloodSugarChartData.add(Entry(1440f, 30f))

        todayDataCounts = 3

        // 데이터 이름 및 Line 설정
        val set = LineDataSet(bloodSugarChartData, "Blood Sugar Levels")
        set.setDrawValues(false)
        set.color = Color.parseColor("#A798E5") // Line의 색상을 설정
        val circleColor = Color.parseColor("#8377D8") // 메인보라
        set.setCircleColors(circleColor) // 데이터 셋의 색상을 설정
        set.highLightColor = Color.TRANSPARENT
        set.mode = LineDataSet.Mode.LINEAR

        lineData = LineData(set)
    }

    private fun initChart() {
        chart.apply {
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)
            data = lineData // 차트에 데이터 설정
            invalidate() // 차트를 다시 그립니다.

            // 좌우 스크롤 및 줌 가능하게 설정
            moveViewToX(0f) // 초기 위치 설정
        }

        // x축 설정
        val xAxis = chart.xAxis
        xAxis.apply {
            setDrawLabels(false) // 라벨 표시 안함
            axisMaximum = 1200f
            axisMinimum = -240f
            granularity = 240f // X축 간격 설정
            textColor = Color.BLACK
            textSize = 0.05f // 텍스트 크기 설정
            position = XAxis.XAxisPosition.BOTTOM // x축 라벨 위치
            setDrawGridLines(false) // Y축 평행 격자선 제거
            setDrawAxisLine(true) // Axis-line 표시
        }

        val yAxis = chart.axisLeft
        yAxis.apply {
            axisMaximum = 350f // 범위를 약간 더 크게 설정
            axisMinimum = 0f
            granularity = 50f // Y축 간격 설정
            setLabelCount(8, true)
            textColor = Color.BLACK
            textSize = 0.3f // 텍스트 크기 설정
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toInt().toString()
                }
            }
        }

        val yRAxis = chart.axisRight
        yRAxis.apply {
            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawGridLines(false)
        }

        chart.description.isEnabled = false
        chart.data = lineData // 데이터 설정

        // Legend 설정 (그래프 제목의 크기를 줄임)
        val legend = chart.legend
        legend.isEnabled = false // 제목을 숨김

        chart.invalidate() // 다시 그리기
    }
}
