package com.epi.epilog.diary

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import api.DiaryFragment
import com.epi.epilog.R
import org.json.JSONException
import org.json.JSONObject

class DiaryFragmentBloodPressure : Fragment(), DiaryFragment {
    private var systolicEditText: EditText? = null
    private var diastolicEditText: EditText? = null
    private var heartRateEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_blood_pressure, container, false)
        systolicEditText = view.findViewById(R.id.input_edit_text_sytolic)
        diastolicEditText = view.findViewById(R.id.input_edit_text_diastolic)
        heartRateEditText = view.findViewById(R.id.input_edit_text_heart_rate)

        // 모든 EditText에 정수만 입력받도록 설정
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source.isEmpty()) {
                return@InputFilter null // Allow deletion
            }
            for (i in start until end) {
                if (!Character.isDigit(source[i])) {
                    Toast.makeText(context, "정수만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                    return@InputFilter "" // Reject non-digits and show toast
                }
            }
            null // Accept the input
        }

        systolicEditText?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(inputFilter)
        }

        diastolicEditText?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(inputFilter)
        }

        heartRateEditText?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(inputFilter)
        }

        return view
    }

    override fun getData(): JSONObject {
        val data = JSONObject()
        try {
            data.put("systolicBloodPressure", systolicEditText?.text.toString())
            data.put("diastolicBloodPressure", diastolicEditText?.text.toString())
            data.put("heartRate", heartRateEditText?.text.toString())
        } catch (e: JSONException) {
            Log.e("final_bloodpressure_error", "JSONException in getData: ${e.message}")
        }

        Log.d("DiaryFragmentBloodPressure", "getData: $data") // 추가된 로그

        return data
    }

    override fun isFilledOut(): Boolean {
        val systolic = systolicEditText?.text?.toString() ?: ""
        val diastolic = diastolicEditText?.text?.toString() ?: ""
        val heartRate = heartRateEditText?.text?.toString() ?: ""
        return systolic.isNotEmpty() || diastolic.isNotEmpty() || heartRate.isNotEmpty()
    }

}
