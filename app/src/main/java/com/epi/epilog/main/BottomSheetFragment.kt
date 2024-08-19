package com.epi.epilog.main

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
import com.epi.epilog.R
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
        val iconContainer = rootView.findViewById<ConstraintLayout>(R.id.main_bsd_yesdiary_layout)

        // 기존에 추가된 뷰가 있다면 제거합니다.
        iconContainer.removeAllViews()

        // 타이틀 텍스트뷰 추가
        val titleTextView = TextView(context)
        titleTextView.id = View.generateViewId()
        titleTextView.text = "일지 선택"
        titleTextView.textSize = 16f
        titleTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        titleTextView.gravity = Gravity.CENTER

        val titleLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        titleLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        titleLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        titleLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        titleLayoutParams.topMargin = dpToPx(15)

        titleTextView.layoutParams = titleLayoutParams
        iconContainer.addView(titleTextView)

        // 날짜 텍스트뷰 추가
        val dateTextView = TextView(context)
        dateTextView.id = View.generateViewId()
        dateTextView.text = formatDate(selectedDate ?: "2024-07-10")  // 선택된 날짜를 사용, 기본값으로 "2024-07-10" 사용
        dateTextView.textSize = 14f
        dateTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        dateTextView.gravity = Gravity.CENTER

        val dateLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        dateLayoutParams.topToBottom = titleTextView.id
        dateLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        dateLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

        dateTextView.layoutParams = dateLayoutParams
        iconContainer.addView(dateTextView)

        // 구분선 이미지뷰 추가
        val lineImageView = ImageView(context)
        lineImageView.id = View.generateViewId()
        lineImageView.setImageResource(R.drawable.dialog_line)

        val lineLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        lineLayoutParams.topToBottom = dateTextView.id
        lineLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        lineLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        lineLayoutParams.topMargin = dpToPx(10)

        lineImageView.layoutParams = lineLayoutParams
        iconContainer.addView(lineImageView)

        var lastViewId = lineImageView.id // 처음에 추가될 뷰는 구분선 아래에 위치하게 됩니다.

        logs.forEach { log ->
            val entryLayout = createDiaryEntryLayout(log)

            // 새로운 LinearLayout을 추가하기 위한 LayoutParams 생성
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.topToBottom = lastViewId
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.setMargins(dpToPx(15), dpToPx(15), dpToPx(15), dpToPx(15)) // 15dp의 마진 추가
            entryLayout.layoutParams = layoutParams

            // 새로운 뷰의 ID를 생성하고 설정
            entryLayout.id = View.generateViewId()
            iconContainer.addView(entryLayout)

            // 현재 추가된 뷰의 ID를 저장하여 다음 뷰의 위치를 설정
            lastViewId = entryLayout.id
        }

        // 일지 추가하기 버튼 추가
        val writeDiaryBtn = Button(context)
        writeDiaryBtn.id = View.generateViewId()
        writeDiaryBtn.text = "일지 추가하기"
        writeDiaryBtn.setBackgroundResource(R.drawable.purple_button_shape)
        writeDiaryBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        writeDiaryBtn.textSize = 16f

        val buttonLayoutParams = ConstraintLayout.LayoutParams(
            dpToPx(300),
            dpToPx(50)
        )
        buttonLayoutParams.topToBottom = lastViewId
        buttonLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        buttonLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        buttonLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        buttonLayoutParams.topMargin = dpToPx(15) // 마지막 아이콘과 버튼 사이에 여백 추가
        buttonLayoutParams.bottomMargin = dpToPx(20)

        writeDiaryBtn.layoutParams = buttonLayoutParams
        iconContainer.addView(writeDiaryBtn)

        writeDiaryBtn.setOnClickListener {
            val intent = Intent(requireContext(), DiaryEditActivity::class.java)
            intent.putExtra("date", selectedDate)
            startActivity(intent)
            dismiss() // 바텀시트 닫기
        }
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

        return diaryEntryView
    }

    // 날짜 포맷 변경 함수
    private fun formatDate(dateString: String): String {
        val parts = dateString.split("-")
        return "${parts[0]}년 ${parts[1]}월 ${parts[2]}일"
    }


    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
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