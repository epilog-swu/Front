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
    private val buttons = mutableListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diary_edit_1)

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
            if (timePicker.visibility == View.GONE) {
                timePicker.visibility = View.VISIBLE
            } else {
                timePicker.visibility = View.GONE
            }
        }

        nextButton.setOnClickListener {
            val intent = Intent(this, DiaryWriteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onButtonClicked(button: Button) {
        if (button.isSelected) {
            button.isSelected = false
            button.setBackgroundResource(R.drawable.seizure_button)
        } else {
            button.isSelected = true
            button.setBackgroundResource(R.drawable.seizure_button)
        }
    }
}
