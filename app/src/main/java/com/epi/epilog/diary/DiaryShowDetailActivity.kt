package com.epi.epilog.diary

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.epi.epilog.R
import com.epi.epilog.api.DiaryDetailResponse
import com.epi.epilog.api.GraphBloodSugarResponse
import com.epi.epilog.api.GraphBloodSugars
import com.epi.epilog.api.GraphWeightBMIDate
import com.epi.epilog.api.GraphWeightBMIResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.databinding.ActivityDiaryShowDetailBinding
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class DiaryShowDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryShowDetailBinding
    private var selectedDate: String? = null // selectedDate를 클래스 변수로 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryShowDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent에서 selectedDate를 받아옴
        selectedDate = intent.getStringExtra("selectedDate") ?: LocalDate.now().toString()

        // 여기에 API 호출과 UI 업데이트를 추가
        fetchDiaryDetails()
    }

    private fun fetchDiaryDetails() {
        // API 호출 후 데이터를 받아오는 로직
        val logId = intent.getIntExtra("id", 0)
        Log.d("DiaryDetail", "logid: $logId")
        val token = getTokenFromSession()  // 토큰 가져오기

        RetrofitClient.retrofitService.getDiaryDetail(logId, token).enqueue(object : Callback<DiaryDetailResponse> {
            override fun onResponse(call: Call<DiaryDetailResponse>, response: Response<DiaryDetailResponse>) {
                if (response.isSuccessful) {
                    val diaryDetail = response.body()
                    diaryDetail?.let {
                        Log.d("DiaryDetail", "Successfully loaded diary details: $it")
                        updateUI(it)
                    }
                } else {
                    Log.e("DiaryDetail", "Failed to load data: ${response.message()}")
                    Toast.makeText(this@DiaryShowDetailActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DiaryDetailResponse>, t: Throwable) {
                Log.e("DiaryDetail", "Error occurred: ${t.message}")
                Toast.makeText(this@DiaryShowDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateUI(diaryDetail: DiaryDetailResponse) {
        // API에서 받은 데이터로 UI 업데이트

        //일지 정보 타이틀 날짜 + 시간
        val formattedTitle = formatTitleDate(diaryDetail.title)
        binding.diaryDetailTitleTv.text = formattedTitle

        //아이콘 추가를 위한 레이아웃 초기화
        binding.iconsLayout.removeAllViews()  // 기존 아이콘 제거

        // keyword에 따른 아이콘 동적 추가
        diaryDetail.keyword.forEach { keyword ->
            val iconResId = getIconResourceForKeyword(keyword)
            if (iconResId != null) {
                val imageView = ImageView(this)
                imageView.setImageResource(iconResId)
                imageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                }
                binding.iconsLayout.addView(imageView)
            }
        }

        // 낙상
        diaryDetail.fall?.let {
            // 주소 텍스트뷰 설정 (필요한 경우)
            binding.diaryDetailFallPlainTv.visibility = View.VISIBLE
            binding.diaryDetailFallAddress.text = it.address
            // 이미지 로딩
            if (!it.mapImage.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.mapImage)
                    .into(binding.diaryDetailFallMapimg)
                binding.diaryDetailFallMapimg.visibility = View.VISIBLE
            } else {
                binding.diaryDetailFallMapimg.visibility = View.GONE
            }
        } ?: run {
            // fall이 null인 경우 낙상관련 요소들을 모조리 숨깁니다
            binding.diaryDetailFallPlainTv.visibility = View.GONE   //'낙상' 글자 숨김
            binding.diaryDetailFallAddress.visibility = View.GONE   //낙상 주소 숨김
            binding.diaryDetailFallMapimg.visibility = View.GONE    //낙상 MAP IMG 숨김
            binding.diaryDetailFallLayout.visibility = View.GONE //상위 레이아웃 숨김
        }


        // 혈당
        if (diaryDetail.bloodSugar.isNullOrEmpty()) {
            binding.diaryDetailBloodsugar.visibility = View.GONE //혈당 칸 없애기
            binding.diaryDetailBloodsugarGraph.visibility = View.GONE //혈당 그래프 없애기
        } else {
            binding.diaryDetailBloodsugartv.text = diaryDetail.bloodSugar
            binding.diaryDetailBloodsugar.visibility = View.VISIBLE
            binding.diaryDetailBloodsugarGraph.visibility = View.VISIBLE
            drawBloodSugarGraph(diaryDetail.bloodSugar)
        }

        // 수축기 혈압
        val isSystolicBPEmpty = diaryDetail.systolicBloodPressure.isNullOrEmpty()
        if (isSystolicBPEmpty) {
            binding.systolicPlainTv.visibility = View.GONE
            binding.systolicBp.visibility = View.GONE
        } else {
            binding.systolicPlainTv.visibility = View.VISIBLE
            binding.systolicBp.text = diaryDetail.systolicBloodPressure
            binding.systolicBp.visibility = View.VISIBLE
        }

        // 이완기 혈압
        val isDiastolicBPEmpty = diaryDetail.diastolicBloodPressure.isNullOrEmpty()
        if (isDiastolicBPEmpty) {
            binding.diastolicPlainTv.visibility = View.GONE
            binding.diastolicBp.visibility = View.GONE
        } else {
            binding.diastolicPlainTv.visibility = View.VISIBLE
            binding.diastolicBp.text = diaryDetail.diastolicBloodPressure
            binding.diastolicBp.visibility = View.VISIBLE
        }

        // 심박수
        val isHeartRateEmpty = diaryDetail.heartRate.isNullOrEmpty()
        if (isHeartRateEmpty) {
            binding.heartRatePlainTv.visibility = View.GONE
            binding.heartRate.visibility = View.GONE
        } else {
            binding.heartRatePlainTv.visibility = View.VISIBLE
            binding.heartRate.text = diaryDetail.heartRate
            binding.heartRate.visibility = View.VISIBLE
        }

        // 상위 레이아웃 visibility 설정
        if (isSystolicBPEmpty && isDiastolicBPEmpty && isHeartRateEmpty) {
            binding.bloodPressureLayout.visibility = View.GONE
        } else {
            binding.bloodPressureLayout.visibility = View.VISIBLE
        }


        // 몸무게
        val isWeightEmpty = diaryDetail.weight == null
        if (isWeightEmpty) {
            binding.diaryDetailWeightPlainTv.visibility = View.GONE
            binding.diaryDetailWeight.visibility = View.GONE
        } else {
            binding.diaryDetailWeight.text = "${diaryDetail.weight}kg"
            binding.diaryDetailWeight.visibility = View.VISIBLE
        }

        // 체지방률
        val isBodyFatPercentageEmpty = diaryDetail.bodyFatPercentage == null
        if (isBodyFatPercentageEmpty) {
            binding.diaryDetailBMIPlainTv.visibility = View.GONE
            binding.diaryDetailBMI.visibility = View.GONE
        } else {
            binding.diaryDetailBMI.text = "${diaryDetail.bodyFatPercentage}%"
            binding.diaryDetailBMI.visibility = View.VISIBLE
        }

        //상위 레이아웃 visibility 설정 //TODO : 나중에 이미지 있으면 상위 레이아웃 나오도록 바꿔줘야 함
        if (isWeightEmpty && isBodyFatPercentageEmpty){
            binding.bodyMeasurementLayout.visibility = View.GONE // 몸무게 및 체지방률 상위 레이아웃 박스
            binding.diaryDetailWeightBMIGraph.visibility = View.GONE
        }else{
            binding.bodyMeasurementLayout.visibility = View.VISIBLE
            binding.diaryDetailWeightBMIGraph.visibility = View.VISIBLE
            drawWeightBMIGraph(selectedDate) //둘 중 하나라도 NULL이 아니면 그래프 작성
        }

//        // 신체 사진
//        val isBodyPhotoEmpty = diaryDetail.bodyPhoto?.isNullOrEmpty()
//        if (isBodyPhotoEmpty == true) {
//            binding.diaryDetailBodyImage.visibility = View.GONE
//            binding.bodyMeasurementLayout.visibility = View.GONE
//        } else {
//            // 신체 사진 로딩
//            // 예: Glide.with(this).load(diaryDetail.bodyPhoto.imageUri).into(binding.bodyImage)
//            binding.diaryDetailBodyImage.visibility = View.GONE //TODO : 일단 안보이도록 설정 ...... XML도 안보이도록 해놨어요
//        }



        // 운동 처리
        val exerciseKeywords = diaryDetail.exercise?.keyword?.filter { it != "직접입력" } ?: emptyList()
        if (exerciseKeywords.isNotEmpty()) {
            val exerciseComment = diaryDetail.exercise?.comment
            binding.exercise.text = exerciseComment

            val exerciseDetails = diaryDetail.exercise?.details
            if (!exerciseDetails.isNullOrBlank()) {
                // details가 있을 경우 상단과 하단에 줄 바꿈 추가
                binding.exerciseDetail.text = "\n$exerciseDetails\n"
                binding.exerciseDetail.visibility = View.VISIBLE
            } else {
                binding.exerciseDetail.visibility = View.GONE
            }


        } else {
            binding.exercise.text = "" // 키워드가 "직접입력"만 있는 경우, 코멘트는 생성하지 않음
            binding.exerciseLayout.visibility = View.GONE
            binding.exerciseDetail.visibility = View.GONE
        }


//        if (diaryDetail.exercise?.keyword?.contains("직접입력") == true) {
//            binding.exerciseDetail.text = diaryDetail.exercise.details
//            binding.exerciseDetail.visibility = View.VISIBLE
//        } else {
//            binding.exerciseDetail.visibility = View.GONE
//        }

        // 운동 배지 추가
        binding.exerciseBadgeLayout.removeAllViews() // 기존 배지 제거

        // 한 줄에 몇 개의 배지를 표시할지 결정합니다.
        val maxBadgesPerRow = 3
        var previousViewId = View.NO_ID
        var previousRowStartViewId = View.NO_ID
        val verticalSpacing = 16 // 상하 간격을 위한 값 (dp)

        exerciseKeywords.forEachIndexed { index, keyword ->
            val badgeTextView = TextView(this).apply {
                text = keyword
                setBackgroundResource(R.drawable.badge)
                setTextColor(ContextCompat.getColor(this@DiaryShowDetailActivity, android.R.color.black))
                textSize = 14f
                setPadding(16, 8, 16, 8)
                id = View.generateViewId()
            }

            // LayoutParams 생성
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            badgeTextView.layoutParams = layoutParams
            binding.exerciseBadgeLayout.addView(badgeTextView)

            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.exerciseBadgeLayout)

            if (index % maxBadgesPerRow == 0) {
                // 새 줄의 시작
                constraintSet.connect(badgeTextView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                if (index == 0) {
                    // 첫 번째 배지
                    constraintSet.connect(badgeTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                } else {
                    // 줄바꿈 시 첫 번째 배지
                    constraintSet.connect(badgeTextView.id, ConstraintSet.TOP, previousRowStartViewId, ConstraintSet.BOTTOM, verticalSpacing)
                    previousRowStartViewId = badgeTextView.id
                }
            } else {
                // 같은 줄에 배치
                constraintSet.connect(badgeTextView.id, ConstraintSet.START, previousViewId, ConstraintSet.END, 8)
                constraintSet.connect(badgeTextView.id, ConstraintSet.TOP, previousViewId, ConstraintSet.TOP)
            }

            constraintSet.applyTo(binding.exerciseBadgeLayout)
            previousViewId = badgeTextView.id

            if (index % maxBadgesPerRow == 0) {
                previousRowStartViewId = badgeTextView.id
            }
        }



        // 기분 처리
        val moodKeywords = diaryDetail.mood?.keyword?.filter { it != "직접입력" } ?: emptyList()
        if (moodKeywords.isNotEmpty()) {
            val moodComment = diaryDetail.mood?.comment
            binding.mood.text = moodComment

            val moodDetails = diaryDetail.mood?.details
            if (!moodDetails.isNullOrBlank()) {
                // details가 있을 경우 상단과 하단에 줄 바꿈 추가
                binding.moodDetail.text = "\n$moodDetails\n"
                binding.moodDetail.visibility = View.VISIBLE
            } else {
                binding.moodDetail.visibility = View.GONE
            }

        } else {
            binding.mood.text = "" // 키워드가 "직접입력"만 있는 경우, 코멘트는 생성하지 않음
            binding.moodDetail.visibility = View.GONE
            binding.moodLayout.visibility = View.GONE // 키워드가 없으면 상위 레이아웃 숨김
        }


        moodKeywords.forEachIndexed { index, keyword ->
            val badgeTextView = TextView(this).apply {
                text = keyword
                setBackgroundResource(R.drawable.badge)
                setTextColor(ContextCompat.getColor(this@DiaryShowDetailActivity, android.R.color.black))
                textSize = 14f
                setPadding(16, 8, 16, 8)
                id = View.generateViewId()
            }

            // LayoutParams 생성
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            badgeTextView.layoutParams = layoutParams
            binding.moodBadgeLayout.addView(badgeTextView)

            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.moodBadgeLayout)

            if (index % maxBadgesPerRow == 0) {
                // 새 줄의 시작
                constraintSet.connect(badgeTextView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                if (index == 0) {
                    // 첫 번째 배지
                    constraintSet.connect(badgeTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                } else {
                    // 줄바꿈 시 첫 번째 배지
                    constraintSet.connect(badgeTextView.id, ConstraintSet.TOP, previousRowStartViewId, ConstraintSet.BOTTOM, verticalSpacing)
                    previousRowStartViewId = badgeTextView.id
                }
            } else {
                // 같은 줄에 배치
                constraintSet.connect(badgeTextView.id, ConstraintSet.START, previousViewId, ConstraintSet.END, 8)
                constraintSet.connect(badgeTextView.id, ConstraintSet.TOP, previousViewId, ConstraintSet.TOP)
            }

            constraintSet.applyTo(binding.moodBadgeLayout)
            previousViewId = badgeTextView.id

            if (index % maxBadgesPerRow == 0) {
                previousRowStartViewId = badgeTextView.id
            }
        }


    }

    private fun formatTitleDate(title: String): String {
        // "2024-08-17 아침식사 전" -> "2024년 08월 17일 아침식사 전"으로 변환
        val parts = title.split(" ")
        val dateParts = parts[0].split("-")  // "2024-08-17"을 분리
        val formattedDate = "${dateParts[0]}년 ${dateParts[1]}월 ${dateParts[2]}일"

        // 시간과 관련된 나머지 부분을 결합하여 반환
        val timePart = parts.subList(1, parts.size).joinToString(" ")
        return "$formattedDate $timePart"
    }


    private fun getIconResourceForKeyword(keyword: String): Int? {
        // 키워드에 따라 아이콘 리소스를 반환하는 로직을 작성합니다.
        return when (keyword) {
            "혈압" -> R.drawable.icon_blood_pressure
            "혈당" -> R.drawable.icon_blood_sugar
            "몸무게" -> R.drawable.icon_weight
            "기분" -> R.drawable.icon_mood
            "운동" -> R.drawable.icon_exercise
            "낙상" -> R.drawable.icon_falldown
            else -> null
        }
    }


    private fun getTokenFromSession(): String {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return "Bearer ${sharedPreferences.getString("AuthToken", "") ?: ""}"
    }

    private fun drawBloodSugarGraph(bloodSugar: String) {
        // 차트 초기화
        initLineChart(binding.root)

        // 혈당 데이터가 유효하면 그래프를 그립니다.
        if (!bloodSugar.isNullOrEmpty()) {
            // 데이터를 차트에 반영하는 함수 호출
            fetchBloodSugars(selectedDate, binding.appGraphBloodSugarChart)
        }
    }

    private fun initLineChart(view: View) {
        val lineChart: LineChart = view.findViewById(R.id.app_graph_blood_sugar_chart)

        // X축 설정
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setGridColor(Color.argb(50, 0, 0, 0))
        xAxis.axisLineColor = Color.parseColor("#817DA1")
        xAxis.textColor = Color.parseColor("#817DA1")
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 7f
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("0", "1", "2", "3", "4", "5", "6", "7"))

        // Y축 설정
        val yAxis = lineChart.axisLeft
        yAxis.axisLineColor = Color.parseColor("#817DA1")
        yAxis.textColor = Color.parseColor("#817DA1")
        yAxis.setGridColor(Color.argb(50, 0, 0, 0))
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 300f

        // 오른쪽 Y축 비활성화
        lineChart.axisRight.isEnabled = false

        // 범례 설정
        val legend = lineChart.legend
        legend.textSize = 12f
        legend.form = Legend.LegendForm.LINE
        legend.isEnabled = false

        // 특정 날짜의 혈당 데이터를 가져와서 차트에 반영
        fetchBloodSugars(selectedDate, lineChart)

        // 차트 갱신
        lineChart.invalidate()
    }

    private fun fetchBloodSugars(date: String?, lineChart: LineChart) {
        val dateString = date.toString()
        Log.d("ActivityDiaryShowDetail", "date String : $dateString")
        val token = getTokenFromSession()

        if (token.isBlank()) {
            Toast.makeText(this, "Auth token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.retrofitService.getGraphBloodSugar(dateString, token).enqueue(object :
            Callback<GraphBloodSugarResponse> {
            override fun onResponse(call: Call<GraphBloodSugarResponse>, response: Response<GraphBloodSugarResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val bloodSugarDatas = responseBody.bloodSugars

                        Log.d("ActivityDiaryShowDetail", "Fetched bloodSugars data: $bloodSugarDatas")
                        updateBloodSugarUI(lineChart, bloodSugarDatas)
                        // TextView 업데이트, 필요시 여기에 추가 가능
                    } else {
                        Toast.makeText(this@DiaryShowDetailActivity, "Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ActivityDiaryShowDetail", "Failed with status code: ${response.code()}, message: ${response.message()}")
                    Toast.makeText(this@DiaryShowDetailActivity, "Failed to load blood sugar data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GraphBloodSugarResponse>, t: Throwable) {
                Toast.makeText(this@DiaryShowDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

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
        dataSet.lineWidth = 3f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val markerView = bloodSugarMarkerView(this, R.layout.graph_marker_layout, entryTitleMap)
        lineChart.marker = markerView

        // 차트 갱신
        lineChart.invalidate()
    }


    private fun drawWeightBMIGraph(selectedDate: String?) {
        // 몸무게 및 체지방률 차트를 초기화
        initLineChart2(binding.root)

        // 해당 월의 데이터를 가져와서 차트에 반영
        fetchGraphWeightBMI(selectedDate)
    }

    private fun initLineChart2(view: View) {
        val lineChart: LineChart = view.findViewById(R.id.graph_weight_bmi_avg_linechart)

        // X축 설정
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawLabels(false)

        // Y축 설정
        val leftAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 150f
        leftAxis.granularity = 30f
        leftAxis.axisLineColor = Color.parseColor("#827DA1")
        leftAxis.textColor = Color.parseColor("#827DA1")

        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false

        // 범례 설정
        val legend = lineChart.legend
        legend.textSize = 12f
        legend.form = Legend.LegendForm.LINE
        legend.isEnabled = false

        // 차트 갱신
        lineChart.invalidate()
    }

    private fun fetchGraphWeightBMI(date: String?) {
        val dateString = date.toString()
        Log.d("ActivityDiaryShowDetail", "date String : $dateString")
        val token = getTokenFromSession()

        if (token.isBlank()) {
            Toast.makeText(this, "Auth token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.retrofitService.getGraphWeightBMI(dateString, token).enqueue(object :
            Callback<GraphWeightBMIResponse> {
            override fun onResponse(call: Call<GraphWeightBMIResponse>, response: Response<GraphWeightBMIResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val weightData = responseBody.dayWeight
                        val bmiData = responseBody.dayBodyFatPercentage

                        Log.d("ActivityDiaryShowDetail", "Fetched weight data: $weightData")
                        Log.d("ActivityDiaryShowDetail", "Fetched BMI data: $bmiData")

                        updateWeightBMIUI(weightData, bmiData)
                    } else {
                        Toast.makeText(this@DiaryShowDetailActivity, "Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ActivityDiaryShowDetail", "Failed with status code: ${response.code()}, message: ${response.message()}")
                    Toast.makeText(this@DiaryShowDetailActivity, "Failed to load weight and BMI data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GraphWeightBMIResponse>, t: Throwable) {
                Toast.makeText(this@DiaryShowDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateWeightBMIUI(weightData: List<GraphWeightBMIDate>, bmiData: List<GraphWeightBMIDate>) {
        val lineChart: LineChart = binding.graphWeightBmiAvgLinechart

        val weightEntries = ArrayList<Entry>()
        val bmiEntries = ArrayList<Entry>()

        for (i in weightData.indices) {
            weightEntries.add(Entry(i.toFloat(), weightData[i].value))
        }

        for (i in bmiData.indices) {
            bmiEntries.add(Entry(i.toFloat(), bmiData[i].value))
        }

        val weightDataSet = LineDataSet(weightEntries, "Weight Line")
        weightDataSet.color = Color.parseColor("#625353")
        weightDataSet.setHighlightEnabled(true)
        weightDataSet.highLightColor = Color.TRANSPARENT
        weightDataSet.setDrawCircles(true)
        weightDataSet.setDrawValues(false)
        weightDataSet.setCircleColors(Color.parseColor("#625353"))
        weightDataSet.lineWidth = 2f

        val bmiDataSet = LineDataSet(bmiEntries, "BMI Line")
        bmiDataSet.color = Color.parseColor("#A096E9")
        bmiDataSet.setHighlightEnabled(true)
        bmiDataSet.highLightColor = Color.TRANSPARENT
        bmiDataSet.setDrawCircles(true)
        bmiDataSet.setDrawValues(false)
        bmiDataSet.setCircleColors(Color.parseColor("#A096E9"))
        bmiDataSet.lineWidth = 2f

        val lineData = LineData(weightDataSet, bmiDataSet)
        lineChart.data = lineData

        val weightMarker = weightMarkerView(this, R.layout.graph_marker_weight_layout, weightData)
        val bmiMarker = bmiMarkerView(this, R.layout.graph_marker_bmi_layout, bmiData)

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

        lineChart.invalidate()
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



}

