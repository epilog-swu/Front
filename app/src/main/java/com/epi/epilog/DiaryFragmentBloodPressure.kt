package com.epi.epilog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment

class DiaryFragmentBloodPressure : Fragment() {

    private var systolicEditText: EditText? = null
    private var diastolicEditText: EditText? = null
    private var heartRateEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_blood_pressure, container, false)
        systolicEditText = view.findViewById(R.id.input_edit_text_systolic)
        diastolicEditText = view.findViewById(R.id.input_edit_text_diastolic)
        heartRateEditText = view.findViewById(R.id.input_edit_text_heart_rate)
        return view
    }

    fun isFilledOut(): Boolean {
        return systolicEditText?.text.toString().isNotEmpty() &&
                diastolicEditText?.text.toString().isNotEmpty() &&
                heartRateEditText?.text.toString().isNotEmpty()
    }
}
