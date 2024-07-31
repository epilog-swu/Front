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

        initWeekCalendarView(view)
        initLineChart(view)
        initLineChart2(view)
        //initClickListeners(view)
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

        // Fetch and display medication checklist for the selected date
        //fetchMedicationChecklist(date)
    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }

    private fun initLineChart(view: View) {
        val lineChart: LineChart = view.findViewById(R.id.app_graph_blood_sugar_chart)
        val entries = mutableListOf<Entry>()

        // 더미 데이터 추가
        entries.add(Entry(0f, 50f))
        entries.add(Entry(1f, 100f))
        entries.add(Entry(2f, 110f))
        entries.add(Entry(3f, 120f))
        entries.add(Entry(4f, 150f))
        entries.add(Entry(5f, 90f))
        entries.add(Entry(6f, 120f))
        entries.add(Entry(7f, 50f))

        val dataSet = LineDataSet(entries, "혈당 변화")
        dataSet.color = Color.BLACK // 데이터 세트의 선 색상 설정
        dataSet.valueTextColor = Color.BLACK // 데이터 값의 텍스트 색상 설정
        dataSet.valueTextSize = 9f //value Text size 조절
        dataSet.setCircleColors(Color.BLACK) // 데이터 dot의 색상 설정
        dataSet.setDrawCircleHole(false) //데이터 dot의 구멍 내부 채움
        dataSet.setHighlightEnabled(true)   // 하이라이트 비활성화
        dataSet.highLightColor = Color.TRANSPARENT // 하이라이트 라인을 투명하게 설정 //마커만 보이게 하기 위함
        dataSet.circleRadius = 5f      //데이터 dot 크기 조절

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // y축 설정
        val yAxis = lineChart.axisLeft
        yAxis.axisLineColor = Color.parseColor("#817DA1") // y축 색상 설정
        yAxis.textColor = Color.parseColor("#817DA1")    // y축 라벨 색상 설정
        yAxis.gridColor = Color.parseColor("#E2DEFC")// y축 격자선 색상 설정
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 300f

        // 오른쪽 y축 비활성화 (필요한 경우)
        lineChart.axisRight.isEnabled = false

        // x축 설정
        val xAxis = lineChart.xAxis
        xAxis.axisLineColor = Color.parseColor("#817DA1") // x축 색상 설정
        xAxis.textColor = Color.parseColor("#817DA1") // x축 라벨 색상 설정
        xAxis.gridColor = Color.parseColor("#E2DEFC")// x축 격자선 색상 설정
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 7f
        xAxis.granularity = 1f  // 라벨 간격 설정
        xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 라벨을 아래쪽에 표시
        xAxis.valueFormatter =
            IndexAxisValueFormatter(arrayOf("0", "1", "2", "3", "4", "5", "6", "7")) // x축 라벨 설정

        lineChart.legend.isEnabled = false //'혈당 변화' 라벨 설정 비활성화

        // 마커 설정
        val markerView = context?.let { bloodSugarMarkerView(it, R.layout.graph_marker_layout) }
        lineChart.marker = markerView
        if (markerView != null) {
            lineChart.marker = markerView
        } else {
            Log.e("GraphPage", "markerView is null")
        }

        // 차트 갱신
        lineChart.invalidate()
    }


    private fun initLineChart2(view: View) {
        val lineChart: LineChart = view.findViewById(R.id.graph_weight_bmi_avg_linechart)

        // X축 설정
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        //xAxis.valueFormatter = IndexAxisValueFormatter(getDaysOfMonth(getDaysInCurrentMonth()))

        // Y축 설정
        val leftAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f
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


        // 특정 날짜의 몸무게와 BMI 데이터를 가져와서 차트에 반영
        fetchGraphWeightBMI(LocalDate.now())

        // 차트 갱신
        lineChart.invalidate()
    }


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
        weightDataSet.setCircleColors(Color.parseColor("#625353"))

        // BMILine 데이터 설정
        val bmiDataSet = LineDataSet(bmiEntries, "BMI Line")
        bmiDataSet.color = Color.parseColor("#A096E9")
        bmiDataSet.setHighlightEnabled(true) // 하이라이트 비활성화
        bmiDataSet.highLightColor = Color.TRANSPARENT // 하이라이트 라인을 투명하게 설정
        bmiDataSet.setDrawCircles(true) // 데이터 dot 그리지 않음
        bmiDataSet.setDrawValues(false) // 데이터 값 텍스트 숨기기
        bmiDataSet.setCircleColors(Color.parseColor("#A096E9"))

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

    // 날짜를 Float로 변환하는 헬퍼 함수
    private fun parseDate(date: String): Int {
        val dateParts = date.split("-")
        return dateParts[2].toInt() // 일(day) 부분을 반환
    }


    // 특정 날짜의 몸무게와 BMI 가져옴
    private fun fetchGraphWeightBMI(date: LocalDate) {
        // LocalDate 객체를 문자열로 변환
        val dateString = date.toString()
        Log.d("GraphWeightBMIFragment", "date String : $dateString")
        // 세션에서 토큰을 가져와 Bearer 토큰 형식으로 설정
        val token = "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6IjIiLCJpYXQiOjE3MjI0MzQ5MDQsImV4cCI6MTgwODgzNDkwNH0.2HCBO4wv2I83XZA7ww7HkHODmROGLp2WTlaj-GLxtTw"

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
        return token
    }


    private fun getDaysInCurrentMonth(): Int {
        val calendar: Calendar = Calendar.getInstance()
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun generateData(daysInMonth: Int): ArrayList<Entry> {
        val data = ArrayList<Entry>()
        for (i in 0 until daysInMonth) {
            data.add(Entry(i.toFloat(), (Math.random() * 200).toFloat()))
        }
        return data
    }

    private fun getDaysOfMonth(daysInMonth: Int): List<String> {
        val days: MutableList<String> = ArrayList()
        for (i in 1..daysInMonth) {
            days.add(i.toString())
        }
        return days
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }


}

//혈당 마커
class bloodSugarMarkerView(context: Context, layoutResource: Int) :
    MarkerView(context, layoutResource) {

    // 레이아웃의 TextView를 초기화

    private val occuranceTypeTV: TextView = findViewById(R.id.main_graph_occurance_markertv)
    private val bloodsugarTV: TextView = findViewById(R.id.main_graph_blood_sugar_markertv)

    // MarkerView가 다시 그려질 때마다 호출되는 콜백으로, UI 내용을 업데이트하는 데 사용
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        occuranceTypeTV.text = "아침식사 전"  // Entry의 y값을 텍스트로 설정합니다. //TODO: 나중에 서버에서 받을 값
        bloodsugarTV.text = "${e?.y}"  // Entry의 y값을 텍스트로 설정합니다.
        super.refreshContent(e, highlight) // 부모 클래스의 메서드 호출
    }

    // MarkerView의 오프셋을 설정하여, 화면에서의 위치를 조정
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
