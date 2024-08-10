package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.epi.epilog.databinding.FragmentMedicineSelectBottom2Binding

class MedicineBottomSheetFragment2 : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.RoundedBottomSheetDialog
    }

    private var _binding: FragmentMedicineSelectBottom2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicineSelectBottom2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.noDiaryWriteDiaryBtn.setOnClickListener {
            val selectedHour = binding.timePicker.hour
            val selectedMinute = binding.timePicker.minute
            // 시간 설정 처리
            Toast.makeText(context, "Selected time: $selectedHour:$selectedMinute", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        view.post {
            val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val scale = resources.displayMetrics.density
                val minHeight = (390 * scale + 0.5f).toInt() // 250dp를 픽셀로 변환

                behavior.peekHeight = minHeight
                bottomSheet.layoutParams.height = minHeight

                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        // 바텀시트 상태 변경 처리
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        val newHeight = (minHeight + (bottomSheet.height - minHeight) * slideOffset).toInt()
                        bottomSheet.layoutParams.height = newHeight
                        bottomSheet.requestLayout()
                    }
                })
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
