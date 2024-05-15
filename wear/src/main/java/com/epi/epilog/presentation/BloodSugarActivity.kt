package com.epi.epilog.presentation

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import com.epi.epilog.R

class BloodSugarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_sugar)

        val radioGroup : RadioGroup = findViewById(R.id.blood_sugar_radio_group)
        val timePicker : TimePicker = findViewById(R.id.blood_sugar_timepicker)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.blood_sugar_btn8) {
                timePicker.visibility = View.VISIBLE
            } else {
                timePicker.visibility = View.GONE
            }
        }
    }
}