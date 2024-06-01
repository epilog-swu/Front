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


        // 프래그먼트에 전달할 번들 생성
        val bundle = Bundle()
        bundle.putString("SELECTED_DATE", selectedDate.toString())

        // 프래그먼트를 생성하고 번들을 설정
        val timeInputFragment = BloodSugarTimeInputFragment()
        timeInputFragment.arguments = bundle

        // Dynamically add fragment
        supportFragmentManager.beginTransaction()
            .add(R.id.blood_sugar_input_form, timeInputFragment)
            .commit()
    }
}