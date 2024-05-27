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
        bloodSugarChartData.add(Entry(720f, 90f))
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

        val xAxis = chart.xAxis
        xAxis.apply {
            setDrawLabels(false)
            axisMaximum = 1200f
            axisMinimum = -240f
            granularity = 240f
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
