package com.epi.epilog.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.main.MainActivity
import com.epi.epilog.R

class signUp5Activity : AppCompatActivity() {

    private lateinit var completeButton: Button
    private lateinit var textViewCode: TextView
    private var token: String? = null  // 토큰을 저장할 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_5)

        completeButton = findViewById(R.id.button5)
        textViewCode = findViewById(R.id.textViewCode)

        // 응답으로 받은 code 값을 텍스트뷰에 표시
        val code = intent.getStringExtra("code") ?: "N/A"
        textViewCode.text = code

        // 토큰을 받아서 변수에 저장
        token = intent.getStringExtra("token")

        val textViewCopy = findViewById<TextView>(R.id.textViewCopy)
        val content = "갤럭시워치에서 연동해주세요"
        val spannableString = SpannableString(content)
        spannableString.setSpan(UnderlineSpan(), 0, content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textViewCopy.text = spannableString

        completeButton.setOnClickListener {
            saveTokenToSession()
            navigateToMain()
        }
    }

    private fun saveTokenToSession() {
        token?.let {
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("AuthToken", it).apply()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
