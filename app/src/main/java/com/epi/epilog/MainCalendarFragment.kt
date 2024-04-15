package com.epi.epilog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.epi.epilog.databinding.FragmentMainCalendarBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener

class MainCalendarFragment : Fragment() {

    // 바인딩 변수 선언. FragmentMainCalendarBinding 형식으로 XML 레이아웃과 연결된 데이터 바인딩 클래스입니다.
    private var binding: FragmentMainCalendarBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment의 뷰를 인플레이트합니다. binding 객체를 통해 레이아웃의 뷰에 접근할 수 있습니다.
        binding = FragmentMainCalendarBinding.inflate(inflater, container, false)
        // 인플레이트된 루트 뷰를 반환합니다.
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MaincalendarView (MaterialCalendarView)에 날짜 선택 리스너를 설정합니다.
        binding?.MaincalendarView?.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->

            // 새 BottomSheetDialog를 생성합니다. 컨텍스트로는 프래그먼트가 속한 액티비티를 사용합니다.
            val bottomSheetDialog = BottomSheetDialog(requireContext())

            // 바텀 시트 다이얼로그의 레이아웃을 인플레이트하여 설정합니다. -> 어떤 뷰를 보여줄건지?
            val bottomSheetView = layoutInflater.inflate(R.layout.fragment_main_bsd_noseizure_, null)
            bottomSheetDialog.setContentView(bottomSheetView)


            // 바텀 시트 다이얼로그가 보여질 때 발생하는 이벤트 리스너를 설정합니다.
            bottomSheetDialog.setOnShowListener {
                // 바텀 시트 뷰를 찾아 레이아웃 파라미터를 조정합니다.
                val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                val layoutParams = bottomSheet?.layoutParams
                // 레이아웃 파라미터의 높이를 1200 픽셀로 설정합니다.
                layoutParams?.height = 1200 // 원하는 높이(단위: 픽셀)
                bottomSheet?.layoutParams = layoutParams
            }

            // 바텀 시트 다이얼로그를 보여줍니다.
            bottomSheetDialog.show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 뷰가 파괴될 때 바인딩 객체를 null로 설정하여 메모리 누수를 방지합니다.
        binding = null
    }
}
