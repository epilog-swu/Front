//package com.epi.epilog.presentation
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.RadioGroup
//import android.widget.TimePicker
//import androidx.fragment.app.Fragment
//import com.epi.epilog.R
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//class BloodSugarTimeInputFragment : Fragment() {
//
//    private lateinit var occurrenceType: String
//    private var selectedDate: String? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//        // 번들에서 날짜 정보 받기
//        val selectedDate = arguments?.getString("SELECTED_DATE")
//        if (selectedDate != null) {
//            Log.d("BloodSugarTimeInputFragment", "Received date in fragment: $selectedDate")
//        } else {
//            Log.e("BloodSugarTimeInputFragment", "No date received in fragment")
//        }
//
//
//        return inflater.inflate(R.layout.fragment_blood_sugar_time_input, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val radioGroup: RadioGroup = view.findViewById(R.id.blood_sugar_radio_group)
//        val timePicker: TimePicker = view.findViewById(R.id.blood_sugar_timepicker)
//
//        radioGroup.setOnCheckedChangeListener { _, checkedId ->
//            timePicker.visibility =
//                if (checkedId == R.id.blood_sugar_btn8) View.VISIBLE else View.GONE
//
//            occurrenceType = when (checkedId) {
//                R.id.blood_sugar_btn1 -> "아침식사 전"
//                R.id.blood_sugar_btn2 -> "아침식사 후"
//                R.id.blood_sugar_btn3 -> "점심식사 전"
//                R.id.blood_sugar_btn4 -> "점심식사 후"
//                R.id.blood_sugar_btn5 -> "저녁식사 전"
//                R.id.blood_sugar_btn6 -> "저녁식사 후"
//                R.id.blood_sugar_btn7 -> "자기 전"
//                R.id.blood_sugar_btn8 -> {
//                    val hour = timePicker.hour
//                    val minute = timePicker.minute
//                    if (selectedDate != null) {
//                        // 날짜와 시간 결합
//                        val dateTime = "$selectedDate $hour:$minute"
//                        Log.d("BloodSugarTimeInputFragment", "Using selected date: $dateTime")
//                        dateTime
//                    } else {
//                        // 현재 날짜 가져오기
//                        val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
//                        val dateTime = "$currentDate $hour:$minute"
//                        Log.d("BloodSugarTimeInputFragment", "Using current date: $dateTime")
//                        dateTime
//                    }
//                }
//                else -> ""
//            }
//        }
//
//        // TimePicker 시간 변경 리스너 설정
//        timePicker.setOnTimeChangedListener { _, hour, minute ->
//            if (radioGroup.checkedRadioButtonId == R.id.blood_sugar_btn8) {
//                // TimePicker에서 선택된 시간을 사용하여 occurrenceType 업데이트
//                if (selectedDate != null) {
//                    // 날짜와 시간 결합
//                    val dateTime = "$selectedDate $hour:$minute"
//                    Log.d("BloodSugarTimeInputFragment", "Using selected date: $dateTime")
//                    occurrenceType = dateTime
//                } else {
//                    // 현재 날짜 가져오기
//                    val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
//                    val dateTime = "$currentDate $hour:$minute"
//                    Log.d("BloodSugarTimeInputFragment", "Using current date: $dateTime")
//                    occurrenceType = dateTime
//                }
//            }
//        }
//
//        view.findViewById<Button>(R.id.blood_sugar_next_btn).setOnClickListener {
//            val bundle = Bundle().apply {
//                putString("occurrenceType", occurrenceType)
//            }
//            val fragment = BloodSugarInputFragment().apply {
//                arguments = bundle
//            }
//
//            activity?.supportFragmentManager?.beginTransaction()?.apply {
//                replace(R.id.blood_sugar_input_form, fragment)
//                addToBackStack(null)
//                commit()
//            }
//        }
//    }
//}
package com.epi.epilog.presentation.blood

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.epi.epilog.presentation.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BloodSugarTimeInputFragment : Fragment() {

    private lateinit var occurrenceType: String
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 번들에서 날짜 정보 받기
        selectedDate = arguments?.getString("SELECTED_DATE")
        if (selectedDate != null) {
            Log.d("BloodSugarTimeInputFragment", "Received date in fragment: $selectedDate")
        } else {
            Log.e("BloodSugarTimeInputFragment", "No date received in fragment")
        }

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
                R.id.blood_sugar_btn8 -> formatDateTime(timePicker.hour, timePicker.minute)
                else -> ""
            }
        }

        // TimePicker 시간 변경 리스너 설정
        timePicker.setOnTimeChangedListener { _, hour, minute ->
            if (radioGroup.checkedRadioButtonId == R.id.blood_sugar_btn8) {
                // TimePicker에서 선택된 시간을 사용하여 occurrenceType 업데이트
                occurrenceType = formatDateTime(hour, minute)
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

    private fun formatDateTime(hour: Int, minute: Int): String {
        val date = selectedDate ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val dateTime = "$date $hour:$minute:00"
        Log.d("BloodSugarTimeInputFragment", "Using date: $dateTime")
        return dateTime
    }
}
