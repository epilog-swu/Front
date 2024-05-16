package com.epi.epilog.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.epi.epilog.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter


class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chart: LineChart

    private val bloodSugarChartData = ArrayList<Entry>() // 데이터 배열
    private lateinit var lineData: LineData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // chart 초기화
        chart = binding.graphBloodSugar

        // 그래프 데이터 설정 및 차트 초기화
        initChartData()
        initChart()


        binding.btnCheckMedicine.setOnClickListener {
            val intent = Intent(
                this,
                MedicineActivity::class.java
            )
            startActivity(intent)
        }
    }

    // 차트 데이터 초기화 메서드
    private fun initChartData() {
        // 일단, 더미데이터
        bloodSugarChartData.add(Entry(-240f, 50f))
        bloodSugarChartData.add(Entry(0f, 100f))
        bloodSugarChartData.add(Entry(240f, 150f))
        bloodSugarChartData.add(Entry(480f, 200f))
        bloodSugarChartData.add(Entry(720f, 250f))
        bloodSugarChartData.add(Entry(960f, 300f))
        bloodSugarChartData.add(Entry(1200f, 350f))

        val set = LineDataSet(bloodSugarChartData, "Blood Sugar Levels")
        set.lineWidth = 2F
        set.setDrawValues(false)
        set.color = Color.BLUE // 데이터 셋의 색상을 설정
        set.highLightColor = Color.TRANSPARENT
        set.mode = LineDataSet.Mode.LINEAR

        lineData = LineData(set)
    }

    private fun initChart() {
        chart.apply {
            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)
            data = lineData // 차트에 데이터 설정
            invalidate() // 차트를 다시 그립니다.
        }

        val xAxis = chart.xAxis
        xAxis.apply {
            setDrawLabels(true)
            axisMaximum = 1200f
            axisMinimum = -240f
            labelCount = 7
            valueFormatter = IndexAxisValueFormatter(arrayOf("1","2","3","4","5","6", "7"))
            textColor = Color.BLACK
            textSize = 1f
            position = XAxis.XAxisPosition.BOTTOM // x축 라벨 위치
            //setDrawLabels(true) // Grid-line 표시
            setDrawGridLines(false) // Y축 평행 격자선 제거
            setDrawAxisLine(true) // Axis-line 표시
        }

        val yAxis = chart.axisLeft
        yAxis.apply {
            axisMaximum = 350f
            axisMinimum = 0f
            val yAxisVals = arrayOf("0", "50", "100", "150", "200")
            textSize = 1f
            valueFormatter = IndexAxisValueFormatter(yAxisVals)
            granularity = 50f
        }

        val yRAxis = chart.axisRight
        yRAxis.apply {
            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawGridLines(false)
        }

        chart!!.description.isEnabled = false
        chart!!.data = lineData //데이터 설정

        chart!!.invalidate() //다시 그리기

    }
}




