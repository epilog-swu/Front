package com.epi.epilog.medicine

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.epi.epilog.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.epi.epilog.databinding.FragmentMedicineSelectBottomBinding

class MedicineBottomSheetFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.RoundedBottomSheetDialog
    }

    private var _binding: FragmentMedicineSelectBottomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicineSelectBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val checklistItemId = arguments?.getInt("checklist_item_id")
        Log.d("MedicineBottomSheetFragment", "Checklist Item ID: $checklistItemId")  // 아이템 ID를 로그로 출력

        val applyChanges = {
            val parentFragment = parentFragment as? MedicineChecklistFragment
            checklistItemId?.let { id ->
                parentFragment?.applyChangesToMedicineItem(id)
                applyStrikeThroughAndChangeBackground(parentFragment, id)
            }
            dismiss()
        }

        binding.bottomButton1.setOnClickListener {
            applyChanges()
        }

        binding.bottomButton2.setOnClickListener {
            applyChanges()
            val secondBottomSheet = MedicineBottomSheetFragment2()
            secondBottomSheet.show(parentFragmentManager, secondBottomSheet.tag)
        }

        binding.bottomButton3.setOnClickListener {
            dismiss() // 아무 변화 없음
        }

        view.post {
            val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val scale = resources.displayMetrics.density
                val minHeight = (150 * scale + 0.5f).toInt()

                behavior.peekHeight = minHeight
                bottomSheet.layoutParams.height = minHeight

                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {}
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        val newHeight = (minHeight + (bottomSheet.height - minHeight) * slideOffset).toInt()
                        bottomSheet.layoutParams.height = newHeight
                        bottomSheet.requestLayout()
                    }
                })
            }
        }
    }

    private fun applyStrikeThroughAndChangeBackground(parentFragment: MedicineChecklistFragment?, checklistItemId: Int) {
        parentFragment?.view?.findViewWithTag<ViewGroup>("item-$checklistItemId")?.let { itemView ->
            val medicineNameTextView = itemView.findViewById<TextView>(R.id.medicine_name)
            val medicineTimeTextView = itemView.findViewById<TextView>(R.id.medicine_time)

            // 텍스트에 밑줄 적용
            medicineNameTextView.paintFlags = medicineNameTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            medicineTimeTextView.paintFlags = medicineTimeTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            // 배경 색상 변경
            itemView.setBackgroundResource(R.drawable.medicine_background)
        } ?: run {
            Log.d("MedicineBottomSheetFragment", "View with tag 'item-$checklistItemId' not found.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

