package com.epi.epilog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
//로그인 페이지로
//        completeButton.setOnClickListener {
//            val intent = Intent(this, LoginPatientActivity::class.java)
//            startActivity(intent)
//            finish()
//        }

        textViewCode.setOnClickListener {
            copyToClipboard(textViewCode.text.toString())
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "코드가 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }
}
