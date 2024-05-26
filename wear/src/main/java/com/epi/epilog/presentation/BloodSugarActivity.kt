package com.epi.epilog.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.R
import java.time.LocalDate

class BloodSugarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_sugar)

        val timeInputFragment = BloodSugarTimeInputFragment()

        //오늘의 날짜 정보 받기
        val selectedDate = intent.getStringExtra("SELECTED_DATE")?.let {
            LocalDate.parse(it)
        }
        if (selectedDate != null) {
            // 날짜 정보를 제대로 받았는지 로그와 토스트 메시지로 확인
            Log.d("BloodSugarActivity", "Received date: $selectedDate")
        } else {
            Log.e("BloodSugarActivity", "No date received")
        }

        // Dynamically add fragment
        supportFragmentManager.beginTransaction()
            .add(R.id.blood_sugar_input_form, timeInputFragment)
            .commit()
    }
}