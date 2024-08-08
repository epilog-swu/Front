package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epi.epilog.databinding.FragmentBsdNodiaryBinding
import com.epi.epilog.databinding.FragmentBsdYesDiaryBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate

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
            bindingYesDiary.btmSheetDlgYesDiaryTitle.text = date
            //아이콘 동적으로 추가
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingNoDiary = null
        _bindingYesDiary = null
    }
}