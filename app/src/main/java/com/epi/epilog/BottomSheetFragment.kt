package com.epi.epilog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.epi.epilog.api.DiaryIconResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.titleKeyWordEntry
import com.epi.epilog.databinding.FragmentBsdNodiaryBinding
import com.epi.epilog.databinding.FragmentBsdYesDiaryBinding
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

        //일지 개수가 0이면
        if (diaryCount == 0){

            // 설정할 텍스트는 arguments에서 가져오거나 다른 방식으로 설정할 수 있습니다.
            val date = arguments?.getString("date")
            bindingNoDiary.btmSheetDlgNoDiaryDate.text = date

            // 추가적인 설정과 클릭 리스너 등 설정
            bindingNoDiary.writeDiaryBtn.setOnClickListener {
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
        }
        //일지 개수가 1개라도 있을 때
        else{
            // 설정할 텍스트는 arguments에서 가져오거나 다른 방식으로 설정할 수 있습니다.
            val date = arguments?.getString("date")
            bindingYesDiary.btmSheetDlgNoDiaryDate.text = date
            if (date != null) {
                fetchDiaryIcons(date)
            }
        }
    }

    private fun fetchDiaryIcons(date: String) {
        val token = getTokenFromSession()
        Log.d("fetchDiaryIcons", "Fetching diary icons for date: $date with token: $token") // date와 token 로그 출력
        RetrofitClient.retrofitService.getDiaryIcon(date, token).enqueue(object :
            Callback<DiaryIconResponse> {
            override fun onResponse(call: Call<DiaryIconResponse>, response: Response<DiaryIconResponse>) {
                if (response.isSuccessful) {
                    val diaryIcons = response.body()
                    diaryIcons?.let {
                        populateIcons(it.logs)
                        Log.d("fetchDiaryIcons", "Response body: $it")
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

    private fun populateIcons(logs: List<titleKeyWordEntry>) {
        val iconContainer = bindingYesDiary.yesDiaryIconContainer
        logs.forEach { log ->
            log.keyword.forEach { keyword ->
                val iconRes = getIconResourceForKeyword(keyword)
                if (iconRes != null) {
                    val imageView = ImageView(context)
                    imageView.setImageResource(iconRes)
                    iconContainer.addView(imageView)
                }
            }
        }
    }


    private fun getIconResourceForKeyword(keyword: String): Int? {
        // 키워드에 따라 아이콘 리소스를 반환하는 로직을 작성합니다.
        return when (keyword) {
            "혈압" -> R.drawable.icon_blood_pressure
            "혈당" -> R.drawable.icon_blood_sugar
            "몸무게" -> R.drawable.icon_weight
            "기분" -> R.drawable.icon_mood
            "운동" -> R.drawable.icon_exercise
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