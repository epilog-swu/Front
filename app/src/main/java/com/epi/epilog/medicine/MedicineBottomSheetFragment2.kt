package com.epi.epilog.medicine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.epi.epilog.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.epi.epilog.databinding.FragmentMedicineSelectBottom2Binding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

            val selectedTime = LocalDateTime.now()
                .withHour(selectedHour)
                .withMinute(selectedMinute)
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"))


            // 선택된 시간을 FragmentResult로 반환
            parentFragmentManager.setFragmentResult(
                "timePickerRequestKey",
                Bundle().apply { putString("selectedTime", selectedTime) }
            )

            dismiss() // 바텀시트 닫기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
