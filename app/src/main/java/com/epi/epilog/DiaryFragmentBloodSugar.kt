package com.epi.epilog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment

class DiaryFragmentBloodSugar : Fragment() {
    private lateinit var bloodSugarEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_blood_sugar, container, false)
        bloodSugarEditText = view.findViewById(R.id.bloodSugarEditText)
        return view
    }

    fun isFilledOut(): Boolean {
        return bloodSugarEditText.text.toString().isNotEmpty()
    }
}
