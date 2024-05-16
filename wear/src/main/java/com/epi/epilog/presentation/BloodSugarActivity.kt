package com.epi.epilog.presentation

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.R

class BloodSugarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_sugar)

        val timeInputFragment = BloodSugarTimeInputFragment()

        // Dynamically add fragment
        supportFragmentManager.beginTransaction()
            .add(R.id.blood_sugar_input_form, timeInputFragment)
            .commit()
    }
}