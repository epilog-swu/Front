package com.epi.epilog.signup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.epi.epilog.R

class signUp2Activity : AppCompatActivity() {

    private lateinit var femaleButton: Button
    private lateinit var maleButton: Button
    private lateinit var nextButton: Button
    private lateinit var nameEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var statureEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_2)

        // Toolbar 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = getString(R.string.signup)

        femaleButton = findViewById(R.id.female_button)
        maleButton = findViewById(R.id.male_button)
        nextButton = findViewById(R.id.button5)
        nameEditText = findViewById(R.id.editTextText)
        weightEditText = findViewById(R.id.weight_edittext)
        statureEditText = findViewById(R.id.stature_edittext)
        femaleButton.setOnClickListener { selectGender(femaleButton, maleButton) }
        maleButton.setOnClickListener { selectGender(maleButton, femaleButton) }

        nextButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "환자의 성함을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                val loginId = intent.getStringExtra("loginId") ?: ""
                val password = intent.getStringExtra("password") ?: ""
                val statureStr = statureEditText.text.toString().trim()
                val weightStr = weightEditText.text.toString().trim()
                val gender = if (femaleButton.isSelected) "여자" else "남자"

                if (statureStr.isEmpty() || weightStr.isEmpty() || !statureStr.matches(Regex("\\d+(\\.\\d+)?")) || !weightStr.matches(Regex("\\d+(\\.\\d+)?"))) {
                    Toast.makeText(this, "키와 몸무게는 숫자를 입력하세요", Toast.LENGTH_SHORT).show()
                } else {
                    val stature = statureStr.toFloat()
                    val weight = weightStr.toFloat()
                    if (loginId.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "로그인 아이디와 비밀번호가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    } else {


                        val intent = Intent(this, signUp3Activity::class.java).apply {
                            putExtra("loginId", loginId)
                            putExtra("password", password)
                            putExtra("name", name)
                            putExtra("stature", stature)
                            putExtra("weight", weight)
                            putExtra("gender", gender)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun selectGender(selectedButton: Button, otherButton: Button) {
        selectedButton.setBackgroundResource(R.drawable.gender_solid_purple_button)
        selectedButton.setTextColor(Color.BLACK)
        otherButton.setBackgroundResource(R.drawable.gender_grey_solid_button)
        otherButton.setTextColor(Color.BLACK)
    }
}
