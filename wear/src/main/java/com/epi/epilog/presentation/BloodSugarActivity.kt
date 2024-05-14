package com.epi.epilog.presentation

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import com.epi.epilog.R
import java.util.EventListener

class BloodSugarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_sugar)

        val timeBtn : Button = findViewById(R.id.blood_sugar_btn8)
        timeBtn.setOnClickListener {
            val timePicker : TimePicker = findViewById(R.id.blood_sugar_timepicker)
            timePicker.visibility = View.VISIBLE
        }
    }
}