package com.epi.epilog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment

class DiaryFragmentExercise : Fragment() {
    private lateinit var checkBoxes: List<CheckBox>
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

    fun isFilledOut(): Boolean {
        return this::checkBoxes.isInitialized && (checkBoxes.any { it.isChecked } || directInputEditText.text.isNotEmpty())
    }
}
