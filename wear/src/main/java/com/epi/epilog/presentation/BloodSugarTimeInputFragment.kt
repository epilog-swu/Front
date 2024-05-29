package com.epi.epilog.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.epi.epilog.R

class BloodSugarTimeInputFragment : Fragment() {

    private lateinit var occurrenceType: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blood_sugar_time_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val radioGroup: RadioGroup = view.findViewById(R.id.blood_sugar_radio_group)
        val timePicker: TimePicker = view.findViewById(R.id.blood_sugar_timepicker)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            timePicker.visibility =
                if (checkedId == R.id.blood_sugar_btn8) View.VISIBLE else View.GONE

            occurrenceType = when (checkedId) {
                R.id.blood_sugar_btn1 -> "아침식사 전"
                R.id.blood_sugar_btn2 -> "아침식사 후"
                R.id.blood_sugar_btn3 -> "점심식사 전"
                R.id.blood_sugar_btn4 -> "점심식사 후"
                R.id.blood_sugar_btn5 -> "저녁식사 전"
                R.id.blood_sugar_btn6 -> "저녁식사 후"
                R.id.blood_sugar_btn7 -> "자기 전"
                R.id.blood_sugar_btn8 -> {
                    val hour = timePicker.hour
                    val minute = timePicker.minute
                    "시간으로 기록하기: $hour:$minute"
                }
                else -> ""
            }
        }

        view.findViewById<Button>(R.id.blood_sugar_next_btn).setOnClickListener {
            val bundle = Bundle().apply {
                putString("occurrenceType", occurrenceType)
            }
            val fragment = BloodSugarInputFragment().apply {
                arguments = bundle
            }

            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.blood_sugar_input_form, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }
}
