package com.epi.epilog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import api.DiaryFragment
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DiaryFragmentExercise : Fragment(), DiaryFragment {
    private lateinit var checkBoxes: List<CheckBox>
    private lateinit var directInputCheckBox: CheckBox
    private lateinit var directInputEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_exercise, container, false)

        checkBoxes = listOf(
            view.findViewById(R.id.checkbox_walk),
            view.findViewById(R.id.checkbox_stretching),
            view.findViewById(R.id.checkbox_yoga),
            view.findViewById(R.id.checkbox_dance),
            view.findViewById(R.id.checkbox_swim),
            view.findViewById(R.id.checkbox_direct_input)
        )

        directInputCheckBox = view.findViewById(R.id.checkbox_direct_input)
        directInputEditText = view.findViewById(R.id.direct_input_text)

        setUpRelativeLayouts(view)

        return view
    }

    private fun setUpRelativeLayouts(view: View) {
        val relativeLayouts = listOf(
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_walk) to view.findViewById<CheckBox>(R.id.checkbox_walk),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_stretching) to view.findViewById<CheckBox>(R.id.checkbox_stretching),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_yoga) to view.findViewById<CheckBox>(R.id.checkbox_yoga),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_dance) to view.findViewById<CheckBox>(R.id.checkbox_dance),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_swim) to view.findViewById<CheckBox>(R.id.checkbox_swim),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_direct_input) to view.findViewById<CheckBox>(R.id.checkbox_direct_input)
        )

        for ((layout, checkBox) in relativeLayouts) {
            layout.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
                updateLayoutBackground(layout, checkBox.isChecked)
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                updateLayoutBackground(layout, isChecked)
                if (checkBox.id == R.id.checkbox_direct_input) {
                    directInputEditText.visibility = if (isChecked) View.VISIBLE else View.GONE
                }
            }

            updateLayoutBackground(layout, checkBox.isChecked)
        }
    }

    private fun updateLayoutBackground(layout: RelativeLayout, isChecked: Boolean) {
        val backgroundRes = if (isChecked) {
            R.drawable.exercise_rounded_background_selected
        } else {
            R.drawable.exercise_rounded_background_unselected
        }
        layout.setBackgroundResource(backgroundRes)
    }

    override fun getData(): JSONObject {
        if (!this::checkBoxes.isInitialized) {
            throw UninitializedPropertyAccessException("checkBoxes has not been initialized")
        }

        val data = JSONObject()
        val exerciseArray = JSONArray()

        try {
            for (checkBox in checkBoxes) {
                if (checkBox.isChecked) {
                    val exerciseObject = JSONObject()
                    exerciseObject.put("type", checkBox.text.toString())
                    exerciseObject.put("details", JSONObject.NULL)
                    exerciseArray.put(exerciseObject)
                }
            }

            if (directInputCheckBox.isChecked) {
                val exerciseObject = JSONObject()
                exerciseObject.put("type", "직접 입력")
                val details = directInputEditText.text.toString()
                exerciseObject.put("details", if (details.isNotEmpty()) details else JSONObject.NULL)
                exerciseArray.put(exerciseObject)
            }

            data.put("exercise", exerciseArray)
        } catch (e: JSONException) {
            Log.e("final_exercise_error", "JSONException in getData: ${e.message}")
        }

        Log.d("DiaryFragmentExercise", "getData: $data") // 추가된 로그

        return data
    }

    override fun isFilledOut(): Boolean {
        return this::checkBoxes.isInitialized && (checkBoxes.any { it.isChecked } || directInputEditText.text.isNotEmpty())
    }

    fun isInitialized(): Boolean {
        return this::checkBoxes.isInitialized && this::directInputCheckBox.isInitialized && this::directInputEditText.isInitialized
    }
}

