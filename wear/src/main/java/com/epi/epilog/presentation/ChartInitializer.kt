//package com.epi.epilog.presentation
//
//import android.graphics.Color
//import com.github.mikephil.charting.charts.LineChart
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.LineData
//import com.github.mikephil.charting.data.LineDataSet
//import com.github.mikephil.charting.formatter.ValueFormatter
//
//class ChartInitializer(private val chart: LineChart) {
//
//    private val bloodSugarChartData = ArrayList<Entry>()
//    private lateinit var lineData: LineData
//
//    fun initChart() {
//        initChartData()
//        setupChart()
//    }
//
//    fun initChartData() {
//        bloodSugarChartData.add(Entry(0f, 50f))
//        bloodSugarChartData.add(Entry(240f, 70f))
//        bloodSugarChartData.add(Entry(480f, 120f))
//        //bloodSugarChartData.add(Entry(720f, 90f))
//        bloodSugarChartData.add(Entry(960f, 250f))
//        bloodSugarChartData.add(Entry(1200f, 300f))
//        bloodSugarChartData.add(Entry(1440f, 30f))
//
//        val set = LineDataSet(bloodSugarChartData, "Blood Sugar Levels")
//        set.setDrawValues(false)
//        set.color = Color.parseColor("#A798E5")
//        val circleColor = Color.parseColor("#8377D8")
//        set.setCircleColors(circleColor)
//        set.highLightColor = Color.TRANSPARENT
//        set.mode = LineDataSet.Mode.LINEAR
//
//        lineData = LineData(set)
//    }
//
//    private fun setupChart() {
//        chart.apply {
//            setDrawGridBackground(false)
//            setBackgroundColor(Color.TRANSPARENT)
//            data = lineData
//            invalidate()
//
//            moveViewToX(0f)
//        }
//
//        //간격 조정을 위한 xInterval 계산
//        val xAxis = chart.xAxis
//        val entryCount = bloodSugarChartData.size
//        val xInterval = if (entryCount > 1) 1440f / (entryCount - 1) else 1440f
//
//        bloodSugarChartData.forEachIndexed { index, entry ->
//            entry.x = index * xInterval
//
//            //bloodSugarChartData는 Entry 객체들의 리스트
//            //각 Entry 객체는 그래프의 데이터 포인트를 나타내며, x값과 y값을 가짐
//            //각 데이터 포인트의 x값을 데이터의 인덱스와 간격(xInterval)에 따라 재설정
//        }
//
//        xAxis.apply {
//            setDrawLabels(false)
//            axisMaximum = 1440f
//            axisMinimum = 0f
//            granularity = xInterval
//            textColor = Color.BLACK
//            textSize = 0.05f
//            position = XAxis.XAxisPosition.BOTTOM
//            setDrawGridLines(false)
//            setDrawAxisLine(true)
//        }
//
//        val yAxis = chart.axisLeft
//        yAxis.apply {
//            axisMaximum = 350f
//            axisMinimum = 0f
//            granularity = 50f
//            setLabelCount(8, true)
//            textColor = Color.BLACK
//            textSize = 0.3f
//            valueFormatter = object : ValueFormatter() {
//                override fun getFormattedValue(value: Float): String {
//                    return value.toInt().toString()
//                }
//            }
//        }
//
//        val yRAxis = chart.axisRight
//        yRAxis.apply {
//            setDrawLabels(false)
//            setDrawAxisLine(false)
//            setDrawGridLines(false)
//        }
//
//        chart.description.isEnabled = false
//        chart.data = lineData
//
//        val legend = chart.legend
//        legend.isEnabled = false
//
//        chart.invalidate()
//    }
//}

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.epi.epilog.presentation.BloodSugarDatas
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class ChartInitializer(private val chart: LineChart) {

    private val bloodSugarChartData = ArrayList<Entry>()
    private lateinit var lineData: LineData
    private lateinit var retrofitService: RetrofitService

    fun initChart() {
        initializeRetrofit() // Retrofit 초기화
        fetchChartData()
    }

    private fun initializeRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun fetchChartData() {
        val token = getTokenFromSession()
        if (token.isNullOrEmpty()) {
            Log.d("ChartInitializer", "Auth token is missing.")
            return
        }

        val call = retrofitService.getBloodSugarDatas("2024-06-04", "Bearer $token")
        call.enqueue(object : Callback<BloodSugarDatas> {
            override fun onResponse(call: Call<BloodSugarDatas>, response: Response<BloodSugarDatas>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.diabetes.isNotEmpty()) {
                            processChartData(it)
                            setupChart()
                        } else {
                            handleNoData()
                        }
                    } ?: run {
                        handleNoData()
                    }
                } else {
                    handleResponseError(response)
                }
            }

            override fun onFailure(call: Call<BloodSugarDatas>, t: Throwable) {
                handleNetworkError(t)
            }
        })
    }

    private fun getTokenFromSession(): String? {
        val sharedPreferences = chart.context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    private fun handleNoData() {
        Log.w("ChartInitializer", "No data available")
    }

    private fun handleResponseError(response: Response<BloodSugarDatas>) {
        val errorMessage = try {
            response.errorBody()?.string()
        } catch (e: IOException) {
            "Unknown error"
        }
        Log.e("ChartInitializer", "Response error: ${response.code()} - $errorMessage")
    }

    private fun handleNetworkError(t: Throwable) {
        Log.e("ChartInitializer", "Network error: ${t.message}", t)
    }

    private fun processChartData(data: BloodSugarDatas) {
        bloodSugarChartData.clear()
        val xInterval = if (data.total > 1) 1440f / (data.total - 1) else 1440f
        data.diabetes.forEachIndexed { index, diabetes ->
            bloodSugarChartData.add(Entry(index * xInterval, diabetes.bloodSugar.toFloat()))
        }
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
        val entryCount = bloodSugarChartData.size
        val xInterval = if (entryCount > 1) 1440f / (entryCount - 1) else 1440f
        bloodSugarChartData.forEachIndexed { index, entry ->
            entry.x = index * xInterval
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
