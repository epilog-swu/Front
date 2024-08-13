package com.epi.epilog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.api.DiaryDetailResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.databinding.ActivityDiaryShowDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityDiaryShowDetail : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryShowDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryShowDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 여기에 API 호출과 UI 업데이트를 추가
        fetchDiaryDetails()
    }

    private fun fetchDiaryDetails() {
        // API 호출 후 데이터를 받아오는 로직
        val logId = intent.getIntExtra("id", 0)
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
                    Toast.makeText(this@ActivityDiaryShowDetail, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DiaryDetailResponse>, t: Throwable) {
                Log.e("DiaryDetail", "Error occurred: ${t.message}")
                Toast.makeText(this@ActivityDiaryShowDetail, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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


        // 혈당
        if (diaryDetail.bloodSugar.isNullOrEmpty()) {
            binding.diaryDetailBloodsugar.visibility = View.GONE //혈당 칸 없애기
            binding.diaryDetailBloodsugarGraph.visibility = View.GONE //혈당 그래프 없애기
        } else {
            binding.diaryDetailBloodsugartv.text = diaryDetail.bloodSugar
            binding.diaryDetailBloodsugar.visibility = View.VISIBLE
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

        // 신체 사진
        val isBodyPhotoEmpty = diaryDetail.bodyPhoto?.isNullOrEmpty()
        if (isBodyPhotoEmpty == true) {
            binding.diaryDetailBodyImage.visibility = View.GONE
        } else {
            // 신체 사진 로딩
            // 예: Glide.with(this).load(diaryDetail.bodyPhoto.imageUri).into(binding.bodyImage)
            binding.diaryDetailBodyImage.visibility = View.VISIBLE
        }

        // 상위 레이아웃 visibility 설정
        if (isWeightEmpty && isBodyFatPercentageEmpty && isBodyPhotoEmpty == true) {
            binding.bodyMeasurementLayout.visibility = View.GONE
            binding.diaryDetailWeightBMIGraph.visibility = View.GONE //몸무게 및 체지방율 그래프도 안보이도록 설정
        } else {
            binding.bodyMeasurementLayout.visibility = View.VISIBLE
        }


        // 운동
        if (diaryDetail.exercise == null || diaryDetail.exercise.keyword.isNullOrEmpty()) {
            binding.exerciseLayout.visibility = View.GONE
        } else {
            binding.exercise.text = diaryDetail.exercise.keyword.toString()
            binding.exerciseLayout.visibility = View.VISIBLE
        }


        // 기분
        if (diaryDetail.mood == null || diaryDetail.mood.keyword.isNullOrEmpty()) {
            binding.moodLayout.visibility = View.GONE
        } else {
            binding.mood.text = diaryDetail.mood.details
            // 키워드와 관련된 배지를 동적으로 추가하는 로직을 구현해야 합니다.
            // 예: addMoodBadges(diaryDetail.mood.keyword)
            binding.moodLayout.visibility = View.VISIBLE
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
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return "Bearer ${sharedPreferences.getString("AuthToken", "") ?: ""}"
    }
}
