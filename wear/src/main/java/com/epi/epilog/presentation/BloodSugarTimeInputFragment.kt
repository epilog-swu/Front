package com.epi.epilog.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.epi.epilog.R
class BloodSugarTimeInputFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blood_sugar_time_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val radioGroup : RadioGroup = view.findViewById(R.id.blood_sugar_radio_group)
        val timePicker : TimePicker = view.findViewById(R.id.blood_sugar_timepicker)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.blood_sugar_btn8) {
                timePicker.visibility = View.VISIBLE
            } else {
                timePicker.visibility = View.GONE
            }
        }

        view.findViewById<Button>(R.id.blood_sugar_next_btn).setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.blood_sugar_input_form, BloodSugarInputFragment())
                addToBackStack(null)  // 뒤로 가기 스택에 추가
                commit()
            }
        }
    }
}