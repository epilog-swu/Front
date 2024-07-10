package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class signUp3Activity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextVerification: EditText
    private lateinit var verifyButton: Button
    private lateinit var confirmButton: Button
    private lateinit var nextButton: Button
    private lateinit var textViewTimer: TextView
    private lateinit var textViewError: TextView
    private var isVerificationSent = false
    private var isVerificationConfirmed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_3)

        editTextName = findViewById(R.id.editTextText)
        editTextPhone = findViewById(R.id.editTextText2)
        editTextVerification = findViewById(R.id.editTextText3)
        verifyButton = findViewById(R.id.verify_button)
        confirmButton = findViewById(R.id.confirm_button)
        nextButton = findViewById(R.id.next_button)
        textViewTimer = findViewById(R.id.textViewTimer)

        verifyButton.setOnClickListener {
            val phone = editTextPhone.text.toString().trim()
            if (phone.matches(Regex("^010\\d{8}\$"))) {
                isVerificationSent = true
                textViewError.visibility = View.GONE
                startVerificationTimer()
                Toast.makeText(this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "올바른 전화번호 11자리를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        confirmButton.setOnClickListener {
            if (isVerificationSent) {
                val verificationCode = editTextVerification.text.toString().trim()
                if (verificationCode.isNotEmpty()) {
                    isVerificationConfirmed = true
                    Toast.makeText(this, "인증되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    textViewError.text = "인증번호를 입력해 주세요."
                    textViewError.visibility = View.VISIBLE
                }
            } else {
                textViewError.text = "인증 버튼 클릭 후 인증번호를 입력해 주세요."
                textViewError.visibility = View.VISIBLE
            }
        }

        nextButton.setOnClickListener {
            if (validateInputs()) {
                val intent = Intent(this, signUp4Activity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "모든 항목을 작성해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = editTextName.text.toString().trim()
        val phone = editTextPhone.text.toString().trim()
        val verification = editTextVerification.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || verification.isEmpty() || !isVerificationConfirmed) {
            return false
        }

        return true
    }

    private fun startVerificationTimer() {
        val timer = object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                textViewTimer.text = String.format("(%02d:%02d)", minutes, seconds)
            }

            override fun onFinish() {
                isVerificationSent = false
                textViewTimer.text = "인증 시간이 만료되었습니다."
            }
        }
        timer.start()
    }
}
