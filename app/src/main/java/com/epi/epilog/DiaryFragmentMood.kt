package com.epi.epilog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment

class DiaryFragmentMood : Fragment() {

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
        val view = inflater.inflate(R.layout.diary_fragment_mood, container, false)

        directInputCheckBox = view.findViewById(R.id.checkbox_direct_input)
        directInputText = view.findViewById(R.id.direct_input_text)

        directInputCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                directInputText.visibility = View.VISIBLE
            } else {
                directInputText.visibility = View.GONE
            }
        }

        return view
    }
}
