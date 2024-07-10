package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DiaryEditActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private var isTimeSelected = false
    private var selectedButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diary_edit_1)

        timePicker = findViewById(R.id.timePicker)
        val timeInputButton: Button = findViewById(R.id.button_time_input)
        val nextButton: Button = findViewById(R.id.button5)

        val gridLayout: GridLayout = findViewById(R.id.gridLayout)
        val buttons = mutableListOf<Button>()




        // 다음으로 버튼 클릭 리스너 설정
        nextButton.setOnClickListener {

                val intent = Intent(this, DiaryWriteActivity::class.java)
                startActivity(intent)

        }
    }
}
