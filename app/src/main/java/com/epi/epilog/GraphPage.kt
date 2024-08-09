package com.epi.epilog


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.epi.epilog.api.GraphBloodSugarAverageResponse
import com.epi.epilog.api.GraphBloodSugarResponse
import com.epi.epilog.api.GraphBloodSugars
import com.epi.epilog.api.GraphWeightBMIDate
import com.epi.epilog.api.GraphWeightBMIResponse
import com.epi.epilog.api.MedicationChecklistResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.RetrofitService
import com.epi.epilog.databinding.FragmentGraphBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class GraphPage : Fragment() {
    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: LocalDate? = null
    private lateinit var weekCalendarView: WeekCalendarView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weekCalendarView = view.findViewById(R.id.graph_calendarview)
        if (weekCalendarView == null) {
            Log.e("GraphPage", "weekCalendarView is null")
            Toast.makeText(context, "Calendar View 초기화 실패", Toast.LENGTH_SHORT).show()
            return
        }

        initWeekCalendarView(view)  //캘린더뷰 초기화
        initLineChart(view)         //혈당 그래프
        initLineChart2(view)        //몸무게 및 체지방률 그래프

        // 앱이 실행될 때 오늘 날짜를 선택한 것처럼 초기화
        val today = LocalDate.now()
        onDateSelected(today)
    }

    private fun initWeekCalendarView(view: View) {
        Log.d("GraphPage", "initWeekCalendarView 시작")

        setupDayBinder()
        setupCalendarView()
        setupDayTitles(view)

        Log.d("GraphPage", "initWeekCalendarView 끝")
    }

    private fun setupDayBinder() {
        weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()
                container.textView.textSize = 14f // 텍스트 크기 변경
                container.textView.setOnClickListener {
                    onDateSelected(data.date)
                }
                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.main_graph_selected_day_bg)
                } else {
                    container.textView.setBackgroundResource(R.drawable.main_graph_plain_day_bg)
                }
            }
        }
    }

    private fun setupCalendarView() {
        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        val startDate = currentMonth.minusMonths(100).atStartOfMonth()
        val endDate = currentMonth.plusMonths(100).atEndOfMonth()
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)

        weekCalendarView.setup(startDate, endDate, daysOfWeek.first())
        weekCalendarView.scrollToWeek(currentDate)
    }

    private fun setupDayTitles(view: View) {
        val titlesContainer = view.findViewById<ViewGroup>(R.id.app_calendar_day_titles_container)
        titlesContainer?.let { container ->
            val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)
            container.children.toList().map { it as TextView }.forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title =
                    dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
                textView.text = title
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6f)

                val layoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginStart = 5
                layoutParams.topMargin = 5
                textView.layoutParams = layoutParams
            } ?: run {
                Toast.makeText(context, "Titles container not found", Toast.LENGTH_SHORT).show()
            }
        } ?: Log.e("GraphPage", "titlesContainer is null")
    }

    private fun onDateSelected(date: LocalDate) {
        val currentSelection = selectedDate
        if (currentSelection == date) {
            selectedDate = null
        } else {
            selectedDate = date
        }

        weekCalendarView.notifyDateChanged(date)
        if (currentSelection != null) {
            weekCalendarView.notifyDateChanged(currentSelection)
        }

        // Show toast message with the selected date
        Toast.makeText(context, "Selected date: $date", Toast.LENGTH_SHORT).show()


        // 선택된 날짜에 따라 차트 업데이트
        view?.let {
            val formattedDate = (selectedDate ?: LocalDate.now()).format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
            val monthDate = (selectedDate ?: LocalDate.now()).format(DateTimeFormatter.ofPattern("MM월"))
            updateDateTextView(formattedDate, monthDate)
            initLineChart(it)
            initLineChart2(it)
        }
        //API를 통해 평균 혈당, 식전후 혈당 데이터 가져와서 UI 업데이트
        fetchAverageBloodSugar(date) { averageBloodSugar, preAverageBloodSugar, postAverageBloodSugar ->
            updateAverageBloodSugarUI(averageBloodSugar, preAverageBloodSugar, postAverageBloodSugar)
        }
    }

    //선택된 날짜에 따라 (혈당)textView 변경
    private fun updateAverageBloodSugarUI(averageBloodSugar: Float, preAverageBloodSugar: Float, postAverageBloodSugar: Float) {
        binding.graphAvgBloodsugarTv.text = averageBloodSugar.toString()
        binding.graphAvgBloodsugarMealPreTv.text = preAverageBloodSugar.toString()
        binding.graphAvgBloodsugarMealPostTv.text = postAverageBloodSugar.toString()
    }


    // 선택된 날짜에 따라 textview 변경
    private fun updateDateTextView(formattedDate: String, monthDate: String) {
        binding.graphDateTv.text = formattedDate // 혈당차트 상단 글자 바꾸기
        binding.graphWeightBmiDateTv.text = monthDate // 몸무게 및 체지방율 차트 상단 글자 바꾸기
    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }

    private fun fetchAverageBloodSugar(date: LocalDate, callback: (Float, Float, Float) -> Unit) {
        val dateString = date.toString()
        Log.d("GraphPage", "Fetching average blood sugar for date: $dateString")

        val token = getTokenFromSession()

        if (token.isBlank()) {
            Toast.makeText(context, "Auth token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("GraphPage", "Using token: $token")

        RetrofitClient.retrofitService.getBloodSugarAverage(dateString, token).enqueue(object :
            Callback<GraphBloodSugarAverageResponse> {
            override fun onResponse(call: Call<GraphBloodSugarAverageResponse>, response: Response<GraphBloodSugarAverageResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val averageBloodSugar = responseBody.average
                        val preAverageBloodSugar = responseBody.preAverage
                        val postAverageBloodSugar = responseBody.postAverage
                        Log.d("GraphPage", "Fetched average blood sugar: $averageBloodSugar")
                        callback(averageBloodSugar, preAverageBloodSugar, postAverageBloodSugar)
                    } else {
                        Toast.makeText(context, "Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("GraphPage", "Failed with status code: ${response.code()}, message: ${response.message()}")
                    Toast.makeText(context, "Failed to load average blood sugar: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GraphBloodSugarAverageResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




    //혈당 데이터 초기화 함수
    private fun initLineChart(view: View) {
        val lineChart: LineChart = view.findViewById(R.id.app_graph_blood_sugar_chart)

        // X축 설정
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setGridColor(Color.argb(50, 0, 0, 0)) // 격자선의 색상을 투명한 검정색으로 설정
        xAxis.axisLineColor = Color.parseColor("#817DA1") // x축 색상 설정
        xAxis.textColor = Color.parseColor("#817DA1") // x축 라벨 색상 설정
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 7f
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("0", "1", "2", "3", "4", "5", "6", "7")) // x축 라벨 설정

        // Y축 설정
        val yAxis = lineChart.axisLeft
        yAxis.axisLineColor = Color.parseColor("#817DA1") // y축 색상 설정
        yAxis.textColor = Color.parseColor("#817DA1")    // y축 라벨 색상 설정
        yAxis.setGridColor(Color.argb(50, 0, 0, 0)) // 격자선의 색상을 투명한 검정색으로 설정
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 300f

        // 오른쪽 Y축 비활성화
        lineChart.axisRight.isEnabled = false

        // 범례 설정
        val legend = lineChart.legend
        legend.textSize = 12f
        legend.form = Legend.LegendForm.LINE
        legend.isEnabled = false

        // 선택된 날짜가 없으면 현재 날짜를 기본값으로 사용
        val dateToFetch = selectedDate ?: LocalDate.now()
        // 특정 날짜의 혈당 데이터를 가져와서 차트에 반영 //혈당 그래프
        fetchBloodSugars(dateToFetch, lineChart)

        // 차트 갱신
        lineChart.invalidate()
    }

    private fun fetchBloodSugars(date: LocalDate, lineChart: LineChart) {
        val dateString = date.toString()
        Log.d("GraphWeightBMIFragment", "date String : $dateString")
        val token = getTokenFromSession()

        if (token.isBlank()) {
            Toast.makeText(context, "Auth token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.retrofitService.getGraphBloodSugar(dateString, token).enqueue(object :
            Callback<GraphBloodSugarResponse> {
            override fun onResponse(call: Call<GraphBloodSugarResponse>, response: Response<GraphBloodSugarResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val bloodSugarDatas = responseBody.bloodSugars

                        Log.d("GraphWeightBMIFragment", "Fetched bloodSugars data: $bloodSugarDatas")
                        // UI를 업데이트하는 함수 호출
                        updateBloodSugarUI(lineChart, bloodSugarDatas)
                        // TextView 업데이트
                        binding.graphDateRecordsCounts.text = " (${bloodSugarDatas.size})"
                    } else {
                        Toast.makeText(context, "Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("GraphWeightBMIFragment", "Failed with status code: ${response.code()}, message: ${response.message()}")
                    Toast.makeText(context, "Failed to load blood sugar data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GraphBloodSugarResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //혈당 데이터 UI 바꾸기
    private fun updateBloodSugarUI(lineChart: LineChart, bloodSugarData: List<GraphBloodSugars>) {
        val entries = ArrayList<Entry>()
        val entryTitleMap = mutableMapOf<Entry, String>()

        for (i in bloodSugarData.indices) {
            val entry = Entry(i.toFloat(), bloodSugarData[i].bloodSugar)
            entries.add(entry)
            entryTitleMap[entry] = bloodSugarData[i].title
        }

        val dataSet = LineDataSet(entries, "혈당 변화")
        dataSet.color = Color.BLACK
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 9f
        dataSet.setCircleColors(Color.BLACK)
        dataSet.setDrawCircleHole(false)
        dataSet.setHighlightEnabled(true)
        dataSet.highLightColor = Color.TRANSPARENT
        dataSet.circleRadius = 5f
        dataSet.lineWidth = 3f // 선의 굵기 설정

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val markerView = context?.let { bloodSugarMarkerView(it, R.layout.graph_marker_layout, entryTitleMap) }
        lineChart.marker = markerView

        // 차트 갱신
        lineChart.invalidate()
    }



    //몸무게 및 체지방률 데이터 초기화 함수
    private fun initLineChart2(view: View) {
        val lineChart: LineChart = view.findViewById(R.id.graph_weight_bmi_avg_linechart)

        // X축 설정
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawLabels(false)
        //xAxis.valueFormatter = IndexAxisValueFormatter(getDaysOfMonth(getDaysInCurrentMonth()))

        // Y축 설정
        val leftAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 150f
        leftAxis.granularity = 30f // 간격 설정
        leftAxis.axisLineColor = Color.parseColor("#827DA1")
        leftAxis.textColor = Color.parseColor("#827DA1")

        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false

        // 범례 설정
        val legend = lineChart.legend
        legend.textSize = 12f
        legend.form = Legend.LegendForm.LINE
        legend.isEnabled = false


        // 선택된 날짜가 없으면 현재 날짜를 기본값으로 사용
        val dateToFetch = selectedDate ?: LocalDate.now()
        // 특정 날짜의 몸무게와 BMI 데이터를 가져와서 차트에 반영
        fetchGraphWeightBMI(dateToFetch)

        // 차트 갱신
        lineChart.invalidate()
    }

    //몸무게 및 체지방률 데이터 UI 바꾸기
    private fun updateWeightBMIUI(weightData: List<GraphWeightBMIDate>, bmiData: List<GraphWeightBMIDate>) {

        // view가 null인 경우 함수 종료
        if (view == null) {
            Log.e("GraphPage", "View is null, cannot initialize chart")
            return
        }

        val lineChart: LineChart = requireView().findViewById(R.id.graph_weight_bmi_avg_linechart)

        val weightEntries = ArrayList<Entry>()
        val bmiEntries = ArrayList<Entry>()

        // weightData와 bmiData를 Entry 리스트로 변환
        for (i in weightData.indices) {
            weightEntries.add(Entry(i.toFloat(), weightData[i].value))
        }

        for (i in bmiData.indices) {
            bmiEntries.add(Entry(i.toFloat(), bmiData[i].value))
        }

        // WeightLine 데이터 설정
        val weightDataSet = LineDataSet(weightEntries, "Weight Line")
        weightDataSet.color = Color.parseColor("#625353")
        weightDataSet.setHighlightEnabled(true) // 하이라이트 비활성화
        weightDataSet.highLightColor = Color.TRANSPARENT // 하이라이트 라인을 투명하게 설정
        weightDataSet.setDrawCircles(true) // 데이터 dot 그리지 않음
        weightDataSet.setDrawValues(false) // 데이터 값 텍스트 숨기기
        weightDataSet.setCircleColors(Color.parseColor("#625353")) //weight 데이터 dot 색상
        weightDataSet.lineWidth = 2f    //weightline 선 굵기

        // BMILine 데이터 설정
        val bmiDataSet = LineDataSet(bmiEntries, "BMI Line")
        bmiDataSet.color = Color.parseColor("#A096E9")
        bmiDataSet.setHighlightEnabled(true) // 하이라이트 비활성화
        bmiDataSet.highLightColor = Color.TRANSPARENT // 하이라이트 라인을 투명하게 설정
        bmiDataSet.setDrawCircles(true) // 데이터 dot 그리지 않음
        bmiDataSet.setDrawValues(false) // 데이터 값 텍스트 숨기기
        bmiDataSet.setCircleColors(Color.parseColor("#A096E9")) //bmi 데이터 dot 색상
        bmiDataSet.lineWidth = 2f       //bmiline 선 굵기

        val lineData = LineData(weightDataSet, bmiDataSet)
        lineChart.data = lineData

        val weightMarker = context?.let { weightMarkerView(it, R.layout.graph_marker_weight_layout, weightData) }
        val bmiMarker = context?.let { bmiMarkerView(it, R.layout.graph_marker_bmi_layout, bmiData) }

        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                if (h.dataSetIndex == 0) {
                    lineChart.marker = weightMarker
                } else {
                    lineChart.marker = bmiMarker
                }
            }

            override fun onNothingSelected() {}
        })

        // 차트 갱신
        lineChart.invalidate()
    }

    // 특정 날짜의 몸무게와 BMI 가져옴
    private fun fetchGraphWeightBMI(date: LocalDate) {
        // LocalDate 객체를 문자열로 변환
        val dateString = date.toString()
        Log.d("GraphWeightBMIFragment", "date String : $dateString")

        val token = getTokenFromSession()

        if (token.isBlank()) {
            Toast.makeText(context, "Auth token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Retrofit을 사용하여 API 호출
        RetrofitClient.retrofitService.getGraphWeightBMI(dateString, token).enqueue(object :
            Callback<GraphWeightBMIResponse> {
            // 응답이 성공적일 때 호출되는 콜백
            override fun onResponse(call: Call<GraphWeightBMIResponse>, response: Response<GraphWeightBMIResponse>) {
                if (response.isSuccessful) {
                    // 응답 본문에서 데이터를 가져옴
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val weightData = responseBody.dayWeight
                        val bmiData = responseBody.dayBodyFatPercentage

                        Log.d("GraphWeightBMIFragment", "Fetched weight data: $weightData")
                        Log.d("GraphWeightBMIFragment", "Fetched BMI data: $bmiData")

                        // UI를 업데이트하는 함수 호출
                        updateWeightBMIUI(weightData, bmiData)
                    } else {
                        Toast.makeText(context, "Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 상태 코드와 응답 메시지를 로그로 출력
                    Log.e("GraphWeightBMIFragment", "Failed with status code: ${response.code()}, message: ${response.message()}")
                    Toast.makeText(context, "Failed to load weight and BMI data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            // 네트워크 호출이 실패했을 때 호출되는 콜백
            override fun onFailure(call: Call<GraphWeightBMIResponse>, t: Throwable) {
                // 실패 메시지를 토스트로 표시
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //토큰 가져오기
    private fun getTokenFromSession(): String {
        val sharedPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("AuthToken", "") ?: ""
        Log.d("GraphWeightBMIFragment", "Fetched token from session: $token")
        return "Bearer $token"  // Ensure token is prefixed with "Bearer "
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }


}

//혈당 마커
class bloodSugarMarkerView(
    context: Context,
    layoutResource: Int,
    private val entryTitleMap: Map<Entry, String>
) : MarkerView(context, layoutResource) {

    private val occuranceTypeTV: TextView = findViewById(R.id.main_graph_occurance_markertv)
    private val bloodsugarTV: TextView = findViewById(R.id.main_graph_blood_sugar_markertv)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val title = entryTitleMap[e]
        occuranceTypeTV.text = title ?: "N/A"  // Entry의 title 값을 텍스트로 설정
        bloodsugarTV.text = "${e?.y}"  // Entry의 y값을 텍스트로 설정
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat() - 20) // 중앙에 표시되도록 오프셋 설정
    }
}


//몸무게 마커
class weightMarkerView(context: Context, layoutResource: Int, private val data: List<GraphWeightBMIDate>) :
    MarkerView(context, layoutResource) {

    private val dateTV: TextView = findViewById(R.id.graph_weight_marker_date)
    private val weightTV: TextView = findViewById(R.id.graph_weight_marker_tv)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val entryIndex = e?.x?.toInt() ?: 0
        val date = data[entryIndex].date
        dateTV.text = date  // 서버에서 받은 날짜 값 설정
        weightTV.text = "${e?.y}"  // Entry의 y값을 텍스트로 설정합니다.
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat() - 20)
    }
}


//체지방률 마커
class bmiMarkerView(context: Context, layoutResource: Int, private val data: List<GraphWeightBMIDate>) :
    MarkerView(context, layoutResource) {

    private val dateTV: TextView = findViewById(R.id.graph_bmi_marker_date)
    private val bmiTV: TextView = findViewById(R.id.graph_bmi_marker_tv)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val entryIndex = e?.x?.toInt() ?: 0
        val date = data[entryIndex].date
        dateTV.text = date  // 서버에서 받은 날짜 값 설정
        bmiTV.text = "${e?.y}"  // Entry의 y값을 텍스트로 설정합니다.
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat() - 20)
    }
}
