package com.epi.epilog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.epi.epilog.api.DiaryIconResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.titleKeyWordEntry
import com.epi.epilog.databinding.FragmentBsdNodiaryBinding
import com.epi.epilog.databinding.FragmentBsdYesDiaryBinding
import com.epi.epilog.diary.ActivityDiaryShowDetail
import com.epi.epilog.diary.DiaryEditActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BottomSheetFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.RoundedBottomSheetDialog
    }


    private var _bindingNoDiary: FragmentBsdNodiaryBinding? = null
    private var _bindingYesDiary: FragmentBsdYesDiaryBinding? = null
    private val bindingNoDiary get() = _bindingNoDiary!!
    private val bindingYesDiary get() = _bindingYesDiary!!

    private var selectedDate: String? = null
    private var diaryCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedDate = it.getString("date")
            diaryCount = it.getInt("diaryCount", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (diaryCount == 0) {
            _bindingNoDiary = FragmentBsdNodiaryBinding.inflate(inflater, container, false)
            bindingNoDiary.root
        } else {
            _bindingYesDiary = FragmentBsdYesDiaryBinding.inflate(inflater, container, false)
            bindingYesDiary.root
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.post {
            //일지 개수가 0이면
            if (diaryCount == 0){

                // 설정할 텍스트는 arguments에서 가져오거나 다른 방식으로 설정할 수 있습니다.
                val formattedDate = selectedDate?.let { formatDate(it) }

                bindingNoDiary.btmSheetDlgNoDiaryDate.text = formattedDate

                // 추가적인 설정과 클릭 리스너 등 설정
                bindingNoDiary.noDiaryWriteDiaryBtn.setOnClickListener {
                    // 일지 추가 버튼 클릭시
                    val intent = Intent(requireContext(), DiaryEditActivity::class.java)
                    intent.putExtra("date", selectedDate)
                    Log.d("bottomsheetFrag", "selectedDate : $selectedDate")
                    startActivity(intent)

                    dismiss() // 바텀시트 닫기
                }

                // 바텀시트 최소 높이 설정 및 콜백 설정
                view.post {
                    val bottomSheet =
                        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    if (bottomSheet != null) {
                        val behavior = BottomSheetBehavior.from(bottomSheet)
                        val scale = resources.displayMetrics.density
                        val minHeight = (250 * scale + 0.5f).toInt() // 250dp를 픽셀로 변환

                        behavior.peekHeight = minHeight
                        //behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        bottomSheet.layoutParams.height = minHeight

                        behavior.addBottomSheetCallback(object :
                            BottomSheetBehavior.BottomSheetCallback() {
                            override fun onStateChanged(bottomSheet: View, newState: Int) {
                                // 바텀시트 상태 변경 처리
                            }

                            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                                // 바텀시트가 슬라이드될 때 호출되는 메서드
                                // 여기서 비율을 유지하면서 높이를 조절할 수 있습니다
                                val newHeight =
                                    (minHeight + (bottomSheet.height - minHeight) * slideOffset).toInt()
                                bottomSheet.layoutParams.height = newHeight
                                bottomSheet.requestLayout()
                            }
                        })
                    }
                }
            } else {
                // 설정할 텍스트는 arguments에서 가져오거나 다른 방식으로 설정할 수 있습니다.
                val date = arguments?.getString("date")
                //bindingYesDiary.btmSheetDlgYesDiaryDate.text = date
                if (date != null) {
                    fetchDiaryIcons(date, view)
                }
            }
        }
    }

    private fun fetchDiaryIcons(date: String, rootView: View) {
        val token = getTokenFromSession()
        RetrofitClient.retrofitService.getDiaryIcon(date, token).enqueue(object :
            Callback<DiaryIconResponse> {
            override fun onResponse(call: Call<DiaryIconResponse>, response: Response<DiaryIconResponse>) {
                if (response.isSuccessful) {
                    val diaryIcons = response.body()
                    // API 호출 성공 시 추가 작업 수행
                    Log.d("fetchDiaryIcons", "API call successful: ${response.message()}")
                    diaryIcons?.let {
                        rootView.post {
                            populateIcons(it.logs, rootView)
                            Log.d("fetchDiaryIcons", "성공 : ${it.logs}")
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to load icons: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DiaryIconResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun populateIcons(logs: List<titleKeyWordEntry>, rootView: View) {
        val scrollContainer = rootView.findViewById<LinearLayout>(R.id.scrollContainer)

        // 기존에 추가된 뷰가 있다면 제거합니다.
        scrollContainer.removeAllViews()

        logs.forEach { log ->
            val entryLayout = createDiaryEntryLayout(log)

            // 항목 사이에 마진을 추가
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 8.toPx(), 0, 0)  // 위쪽에만 8dp 마진 추가
            entryLayout.layoutParams = layoutParams

            scrollContainer.addView(entryLayout)
        }
    }

    // dp를 px로 변환하는 확장 함수
    private fun Int.toPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density + 0.5f).toInt()
    }




    private fun createDiaryEntryLayout(log: titleKeyWordEntry): LinearLayout {
        val inflater = LayoutInflater.from(context)
        val diaryEntryView = inflater.inflate(R.layout.yes_diary_count_layout, null, false) as LinearLayout

        val titleTextView = diaryEntryView.findViewById<TextView>(R.id.yes_diary_icon_title)
        titleTextView.text = log.title

        // 아이콘들이 가로로 배열되도록 수정
        val iconContainer = diaryEntryView.findViewById<LinearLayout>(R.id.yes_diary_icon_container)
        iconContainer.orientation = LinearLayout.HORIZONTAL
        iconContainer.gravity = Gravity.CENTER // 아이콘 중앙 정렬

        log.keyword.forEach { keyword ->
            val iconRes = getIconResourceForKeyword(keyword)
            if (iconRes != null) {
                val imageView = ImageView(context)
                imageView.setImageResource(iconRes)
                val imageLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                imageLayoutParams.setMargins(8, 0, 8, 0)  // 아이콘 사이에 여백 추가
                imageView.layoutParams = imageLayoutParams
                iconContainer.addView(imageView)
            }
        }

        // 여기서 클릭 리스너 추가
        diaryEntryView.setOnClickListener {
            val intent = Intent(requireContext(), ActivityDiaryShowDetail::class.java)
            intent.putExtra("id", log.id)  // 선택된 일지의 id 정보를 전달
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
            dismiss()  // 바텀시트를 닫음
        }

        Log.d("createDiaryEntryLayout", "Created layout for: ${log.title}")
        return diaryEntryView
    }

    // 날짜 포맷 변경 함수
    private fun formatDate(dateString: String): String {
        val parts = dateString.split("-")
        return "${parts[0]}년 ${parts[1]}월 ${parts[2]}일"
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
        val sharedPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("AuthToken", "") ?: ""
        return "Bearer $token"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingNoDiary = null
        _bindingYesDiary = null
    }
}