package com.epi.epilog.diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DiaryEditActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private var selectedButton: Button? = null
    private val buttons = mutableListOf<Button>()
    private var occurrenceType: String? = null
    private var selectedTime: String? = null
    private var isTimePickerVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diary_edit_1)

        // 날짜 정보 받기
        val selectedDate = intent.getStringExtra("date")
        Log.d("diaryEditActivity","selectedDate : $selectedDate")

        timePicker = findViewById(R.id.timePicker)
        val timeInputButton: Button = findViewById(R.id.button_time_input)
        val nextButton: Button = findViewById(R.id.button5)

        val gridLayout: GridLayout = findViewById(R.id.gridLayout)
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child is Button) {
                buttons.add(child)
                child.setOnClickListener { onButtonClicked(child) }
            }
        }

        timeInputButton.setOnClickListener {
            isTimePickerVisible = if (timePicker.visibility == View.GONE) {
                timePicker.visibility = View.VISIBLE
                onButtonClicked(timeInputButton) // 시간 입력 버튼 선택 처리
                true
            } else {
                timePicker.visibility = View.GONE
                false
            }

        }

        //TODO : 시간 입력 시 occuranceType  null로 보내야하는지, 그리고 그냥 버튼 클릭시 time null로 보내야 하는지
        nextButton.setOnClickListener {
            val intent = Intent(this, DiaryWriteActivity::class.java)
            if (isTimePickerVisible) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    set(Calendar.MINUTE, timePicker.minute)
                }
                val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
                selectedTime = "$selectedDate $dateTime"
                intent.putExtra("time", selectedTime)
            } else {
                intent.putExtra("occurrenceType", occurrenceType)
            }
            intent.putExtra("date", selectedDate)
            // Intent 내용 출력
            Log.d("DiaryEditActivity", "Intent data: date=${intent.getStringExtra("date")}, occurrenceType=${intent.getStringExtra("occurrenceType")}, time=${intent.getStringExtra("time")}")
            startActivity(intent)
        }
    }

    private fun onButtonClicked(button: Button) {
        selectedButton?.isSelected = false
        button.isSelected = true
        selectedButton = button
        occurrenceType = button.text.toString()
    }
}
