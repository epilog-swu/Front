package com.epi.epilog.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.ramotion.cardslider.CardSliderLayoutManager
import com.ramotion.cardslider.CardSnapHelper
import com.epi.epilog.R

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chart: LineChart

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


        // initCountryText()
        // initSwitchers()
        // initGreenDot()
    }





    // 차트 데이터 초기화 메서드
    private fun initChartData() {
        // 일단, 더미데이터
        // x는 그냥 길이, y는 실제 수치라고 생각하면 됩니다.
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

        // 색상
        set.color = Color.parseColor("#A798E5") // Line의 색상을 설정
        val circleColor = Color.parseColor("#8377D8") // 메인보라
        set.setCircleColors(circleColor) // 데이터 셋의 색상을 설정

        // 강조 표시
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
            // setDragEnabled(true)
            // setScaleEnabled(true)
            // setPinchZoom(true)
            // setVisibleXRangeMaximum(7f) // 한 번에 보이는 최대 x축 범위 설정
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
                    return value.toInt().toString()
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


    private fun onActiveCardChange() {
        // 여기에 카드 변경 시 처리할 로직을 추가하세요
    }
}
