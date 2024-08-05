package com.epi.epilog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import api.DiaryFragment
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DiaryFragmentMood : Fragment(), DiaryFragment {
    private lateinit var checkBoxes: List<CheckBox>
    private lateinit var directInputCheckBox: CheckBox
    private lateinit var directInputEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_mood, container, false)

        checkBoxes = listOf(
            view.findViewById(R.id.mood_calm),
            view.findViewById(R.id.mood_happy),
            view.findViewById(R.id.moodCheckbox_directInput)
        )

        directInputCheckBox = view.findViewById(R.id.moodCheckbox_directInput)
        directInputEditText = view.findViewById(R.id.mood_direct_input_text)

        directInputCheckBox.setOnCheckedChangeListener { _, isChecked ->
            directInputEditText.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        return view
    }

    override fun getData(): JSONObject {
        if (!this::checkBoxes.isInitialized || !this::directInputCheckBox.isInitialized || !this::directInputEditText.isInitialized) {
            throw UninitializedPropertyAccessException("Properties have not been initialized")
        }

        val data = JSONObject()
        val moodArray = JSONArray()

        try {
            for (checkBox in checkBoxes) {
                if (checkBox.isChecked) {
                    val exerciseObject = JSONObject()
                    exerciseObject.put("type", checkBox.text.toString())
                    exerciseObject.put("details", JSONObject.NULL)
                    moodArray.put(exerciseObject)
                }
            }

            if (directInputCheckBox.isChecked) {
                val exerciseObject = JSONObject()
                exerciseObject.put("type", "직접 입력")
                val details = directInputEditText.text.toString()
                exerciseObject.put("details", if (details.isNotEmpty()) details else JSONObject.NULL)
                moodArray.put(exerciseObject)
            }

            data.put("mood", moodArray)
        } catch (e: JSONException) {
            Log.e("final_mood_error", "JSONException in getData: ${e.message}")
        }

        Log.d("DiaryFragmentMood", "getData: $data") // 추가된 로그

        return data
    }

    override fun isFilledOut(): Boolean {
        return this::checkBoxes.isInitialized && (checkBoxes.any { it.isChecked } || directInputEditText.text.isNotEmpty())
    }

    fun isInitialized(): Boolean {
        return this::checkBoxes.isInitialized && this::directInputCheckBox.isInitialized && this::directInputEditText.isInitialized
    }
}
