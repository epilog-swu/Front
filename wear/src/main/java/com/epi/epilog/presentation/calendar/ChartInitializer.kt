package com.epi.epilog.presentation.calendar

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.epi.epilog.presentation.blood.BloodSugarDatas
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.XAxis
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDate

class ChartInitializer(private val chart: LineChart) {

    private val bloodSugarChartData = ArrayList<Entry>()
    private lateinit var lineData: LineData
    private lateinit var retrofitService: RetrofitService

    fun initChart() {
        initializeRetrofit() // Retrofit 초기화
    }

    fun updateChart(date: LocalDate) {
        fetchChartData(date)
    }

    private fun initializeRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun fetchChartData(date: LocalDate) {
        val token = getTokenFromSession()
        if (token.isNullOrEmpty()) {
            Log.d("ChartInitializer", "Auth token is missing.")
            return
        }

        val call = retrofitService.getBloodSugarDatas(date.toString(), "Bearer $token")
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
        bloodSugarChartData.clear()
        chart.clear()
        chart.invalidate()

        // 데이터가 없을 때 설정할 속성들
        chart.setNoDataText("혈당 데이터가 없습니다.")
        chart.invalidate()
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
