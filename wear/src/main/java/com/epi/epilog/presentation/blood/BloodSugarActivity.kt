package com.epi.epilog.presentation.blood

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.presentation.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

        // 날짜 정보를 "00월 00일 혈당기록" 형태로 변환
        val formattedDate = selectedDate?.let {
            it.format(DateTimeFormatter.ofPattern("MM월 dd일"))
        }
        val recordTitle = "$formattedDate 혈당기록"

        // TextView 업데이트
        val textView: TextView = findViewById(R.id.blood_sugar_title)
        textView.text = recordTitle



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