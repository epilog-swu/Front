package com.epi.epilog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class signUp5Activity : AppCompatActivity() {

    private lateinit var completeButton: Button
    private lateinit var textViewCode: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_5)

        completeButton = findViewById(R.id.complete_button)
        textViewCode = findViewById(R.id.textViewCode)

        val textViewCopy = findViewById<TextView>(R.id.textViewCopy)
        val content = "갤럭시워치에서 연동해주세요"
        val spannableString = SpannableString(content)
        spannableString.setSpan(UnderlineSpan(), 0, content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textViewCopy.text = spannableString

        completeButton.setOnClickListener {
            val intent = Intent(this, startActivity::class.java)
            startActivity(intent)
        }
    }
}
