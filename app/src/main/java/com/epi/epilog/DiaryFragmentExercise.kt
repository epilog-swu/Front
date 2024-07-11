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
        return view
    }

    fun isFilledOut(): Boolean {
        return checkBoxes.any { it.isChecked }
    }
}
