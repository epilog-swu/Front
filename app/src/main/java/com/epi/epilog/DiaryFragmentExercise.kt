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

    private lateinit var directInputCheckBox: CheckBox
    private lateinit var directInputText: EditText
    fun isFilledOut(): Boolean {
        // Implement your validation logic here
        return true
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_exercise, container, false)

        directInputCheckBox = view.findViewById(R.id.checkbox_direct_input)
        directInputText = view.findViewById(R.id.direct_input_text)

        setUpRelativeLayouts(view)

        directInputCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                directInputText.visibility = View.VISIBLE
            } else {
                directInputText.visibility = View.GONE
            }
        }

        return view
    }

    private fun setUpRelativeLayouts(view: View) {
        val relativeLayouts = listOf(
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_walk) to view.findViewById<CheckBox>(R.id.checkbox_walk),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_stretching) to view.findViewById<CheckBox>(R.id.checkbox_stretching),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_yoga) to view.findViewById<CheckBox>(R.id.checkbox_yoga),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_dance) to view.findViewById<CheckBox>(R.id.checkbox_dance),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_swim) to view.findViewById<CheckBox>(R.id.checkbox_swim),
            view.findViewById<RelativeLayout>(R.id.checkbox_layout_direct_input) to directInputCheckBox
        )

        for ((layout, checkBox) in relativeLayouts) {
            layout.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                updateLayoutBackground(layout, isChecked)
            }

            // Initial background setup
            updateLayoutBackground(layout, checkBox.isChecked)
        }
    }

    private fun updateLayoutBackground(layout: RelativeLayout, isChecked: Boolean) {
        val backgroundRes = if (isChecked) {
            R.drawable.exercise_true
        } else {
            R.drawable.exercise_false
        }
        layout.setBackgroundResource(backgroundRes)
    }
}
