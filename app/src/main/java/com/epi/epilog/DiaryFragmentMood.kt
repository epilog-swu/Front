package com.epi.epilog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment

class DiaryFragmentMood : Fragment() {
    private lateinit var checkBoxes: List<CheckBox>
    private lateinit var directInputEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_mood, container, false)
        checkBoxes = listOf(
            view.findViewById(R.id.mood_1),
            view.findViewById(R.id.mood_2),
            view.findViewById(R.id.checkbox_direct_input)
        )
        directInputEditText = view.findViewById(R.id.direct_input_text)

        val directInputCheckBox = view.findViewById<CheckBox>(R.id.checkbox_direct_input)
        directInputCheckBox.setOnCheckedChangeListener { _, isChecked ->
            directInputEditText.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        return view
    }

    fun isFilledOut(): Boolean {
        return this::checkBoxes.isInitialized && (checkBoxes.any { it.isChecked } || directInputEditText.text.isNotEmpty())
    }
}
