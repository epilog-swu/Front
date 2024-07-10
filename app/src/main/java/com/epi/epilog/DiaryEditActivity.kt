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

        // 모든 시간대 버튼을 가져옴
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child is Button) {
                buttons.add(child)
            }
        }

        buttons.forEach { button ->
            button.setOnClickListener {
                isTimeSelected = true
                selectedButton?.isSelected = false
                button.isSelected = true
                selectedButton = button
                if (button == timeInputButton) {
                    timePicker.visibility = View.VISIBLE
                } else {
                    timePicker.visibility = View.GONE
                }
            }
        }

        // 다음으로 버튼 클릭 리스너 설정
        nextButton.setOnClickListener {
            if (isTimeSelected) {
                val intent = Intent(this, DiaryWriteActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "시간대를 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
