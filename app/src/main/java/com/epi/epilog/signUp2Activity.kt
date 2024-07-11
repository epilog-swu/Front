package com.epi.epilog

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class signUp2Activity : AppCompatActivity() {

    private lateinit var femaleButton: Button
    private lateinit var maleButton: Button
    private lateinit var nextButton: Button
    private lateinit var nameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_2)

        // findViewById로 뷰를 찾습니다.
        femaleButton = findViewById(R.id.female_button)
        maleButton = findViewById(R.id.male_button)
        nextButton = findViewById(R.id.next_button)
        nameEditText = findViewById(R.id.editTextText) // 이름 입력 EditText

        femaleButton.setOnClickListener {
            selectGender(femaleButton, maleButton)
        }

        maleButton.setOnClickListener {
            selectGender(maleButton, femaleButton)
        }

        nextButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "환자의 성함을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, signUp3Activity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun selectGender(selectedButton: Button, otherButton: Button) {
        selectedButton.setBackgroundResource(R.drawable.gender_solid_purple_button) // gender_solid_purple_shape.xml로 배경 변경
        selectedButton.setTextColor(Color.BLACK)

        otherButton.setBackgroundResource(R.drawable.gender_grey_solid_button) // 기존의 회색 배경으로 변경
        otherButton.setTextColor(Color.BLACK)
    }

}