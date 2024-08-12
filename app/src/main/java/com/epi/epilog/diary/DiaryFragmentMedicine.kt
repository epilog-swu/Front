package com.epi.epilog.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.epi.epilog.R

class DiaryFragmentMedicine : Fragment() {

    private lateinit var medicationEditText: EditText
    private lateinit var addButton: Button
    private lateinit var medicationList: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_medicine, container, false)

        medicationEditText = view.findViewById(R.id.medicationEditText)
        addButton = view.findViewById(R.id.addButton)
        medicationList = view.findViewById(R.id.medicationList)

        addButton.setOnClickListener {
            addMedicationItem(medicationEditText.text.toString())
        }

        return view
    }

    private fun addMedicationItem(medicationName: String) {
        if (medicationName.isNotEmpty()) {
            val medicationItem = LayoutInflater.from(context).inflate(R.layout.medication_item, medicationList, false) as ConstraintLayout

            val medicationNameTextView = medicationItem.findViewById<TextView>(R.id.medicationName)
            val decreaseButton = medicationItem.findViewById<Button>(R.id.decreaseButton)
            val increaseButton = medicationItem.findViewById<Button>(R.id.increaseButton)
            val quantityText = medicationItem.findViewById<TextView>(R.id.quantityText)
            val removeButton = medicationItem.findViewById<ImageView>(R.id.removeButton)

            medicationNameTextView.text = medicationName
            var quantity = 1

            decreaseButton.setOnClickListener {
                if (quantity > 1) {
                    quantity -= 1
                    quantityText.text = "$quantity 정"
                } else {
                    Toast.makeText(requireContext(), "최소 1 정 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                }
            }

            increaseButton.setOnClickListener {
                quantity += 1
                quantityText.text = "$quantity 정"
            }

            removeButton.setOnClickListener {
                medicationList.removeView(medicationItem)
            }

            quantityText.text = "$quantity 정"
            medicationList.addView(medicationItem)
            medicationEditText.text.clear()
        } else {
            Toast.makeText(requireContext(), "약 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    fun isFilledOut(): Boolean {
        return ::medicationList.isInitialized && medicationList.childCount > 0
    }

    fun getMedicationListCount(): Int {
        return if (::medicationList.isInitialized) medicationList.childCount else 0
    }


}
