package com.epi.epilog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.bottomButton2.setOnClickListener {
            val secondBottomSheet = MedicineBottomSheetFragment2()
            secondBottomSheet.show(parentFragmentManager, secondBottomSheet.tag)
            dismiss()
        }

        view.post {
            val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val scale = resources.displayMetrics.density
                val minHeight = (210 * scale + 0.5f).toInt()

                behavior.peekHeight = minHeight
                bottomSheet.layoutParams.height = minHeight

                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
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
