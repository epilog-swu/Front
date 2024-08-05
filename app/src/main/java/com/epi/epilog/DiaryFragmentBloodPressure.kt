package com.epi.epilog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import api.DiaryFragment
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
        systolicEditText = view.findViewById(R.id.input_edit_text_systolic)
        diastolicEditText = view.findViewById(R.id.input_edit_text_diastolic)
        heartRateEditText = view.findViewById(R.id.input_edit_text_heart_rate)
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
