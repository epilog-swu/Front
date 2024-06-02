package com.epi.epilog.presentation

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

class ChartInitializer(private val chart: LineChart) {

    private val bloodSugarChartData = ArrayList<Entry>()
    private lateinit var lineData: LineData

    fun initChart() {
        initChartData()
        setupChart()
    }

    fun initChartData() {
        bloodSugarChartData.add(Entry(0f, 50f))
        bloodSugarChartData.add(Entry(240f, 70f))
        bloodSugarChartData.add(Entry(480f, 120f))
        //bloodSugarChartData.add(Entry(720f, 90f))
        bloodSugarChartData.add(Entry(960f, 250f))
        bloodSugarChartData.add(Entry(1200f, 300f))
        bloodSugarChartData.add(Entry(1440f, 30f))

        val set = LineDataSet(bloodSugarChartData, "Blood Sugar Levels")
        set.setDrawValues(false)
        set.color = Color.parseColor("#A798E5")
        val circleColor = Color.parseColor("#8377D8")
        set.setCircleColors(circleColor)
        set.highLightColor = Color.TRANSPARENT
        set.mode = LineDataSet.Mode.LINEAR

        lineData = LineData(set)
    }

    private fun setupChart() {
        chart.apply {
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)
            data = lineData
            invalidate()

            moveViewToX(0f)
        }

        //간격 조정을 위한 xInterval 계산
        val xAxis = chart.xAxis
        val entryCount = bloodSugarChartData.size
        val xInterval = if (entryCount > 1) 1440f / (entryCount - 1) else 1440f

        bloodSugarChartData.forEachIndexed { index, entry ->
            entry.x = index * xInterval

            //bloodSugarChartData는 Entry 객체들의 리스트
            //각 Entry 객체는 그래프의 데이터 포인트를 나타내며, x값과 y값을 가짐
            //각 데이터 포인트의 x값을 데이터의 인덱스와 간격(xInterval)에 따라 재설정
        }

        xAxis.apply {
            setDrawLabels(false)
            axisMaximum = 1440f
            axisMinimum = 0f
            granularity = xInterval
            textColor = Color.BLACK
            textSize = 0.05f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(true)
        }

        val yAxis = chart.axisLeft
        yAxis.apply {
            axisMaximum = 350f
            axisMinimum = 0f
            granularity = 50f
            setLabelCount(8, true)
            textColor = Color.BLACK
            textSize = 0.3f
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
        chart.data = lineData

        val legend = chart.legend
        legend.isEnabled = false

        chart.invalidate()
    }
}
