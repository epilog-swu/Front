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

class DiaryFragmentBloodSugar: Fragment(), DiaryFragment {
    private lateinit var bloodSugarEditText: EditText

    private var selectedDate: String? = null
    private var occurrenceType: String? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedDate = it.getString("date")
            occurrenceType = it.getString("occurrenceType")
            selectedTime = it.getString("time")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.diary_fragment_blood_sugar, container, false)
        bloodSugarEditText = view.findViewById(R.id.bloodSugarEditText)

        // 초기화 후에만 필터를 설정
        if (::bloodSugarEditText.isInitialized) {
            bloodSugarEditText.inputType = InputType.TYPE_CLASS_NUMBER
            bloodSugarEditText.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
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
            })
        }

        return view
    }

    override fun getData(): JSONObject {
        val data = JSONObject()
        try {
            data.put("bloodSugar", bloodSugarEditText.text.toString())
        } catch (e: JSONException) {
            Log.e("final_bloodsugar_error", "JSONException in getData: ${e.message}")
        }

        Log.d("DiaryFragmentBloodSugar", "getData: $data") // 추가된 로그
        return data
    }

    override fun isFilledOut(): Boolean {
        val bloodSugarEditText = bloodSugarEditText?.text?.toString() ?: ""
        return bloodSugarEditText.isNotEmpty()
    }

    companion object {
        fun newInstance(date: String?, occurrenceType: String?, time: String?): DiaryFragmentBloodSugar {
            val fragment = DiaryFragmentBloodSugar()
            val args = Bundle()
            args.putString("date", date)
            args.putString("occurrenceType", occurrenceType)
            args.putString("time", time)
            fragment.arguments = args
            return fragment
        }
    }
}
