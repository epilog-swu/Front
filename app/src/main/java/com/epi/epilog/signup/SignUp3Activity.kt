package com.epi.epilog.signup

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.epi.epilog.R

class signUp3Activity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextVerification: EditText
    private lateinit var verifyButton: Button
    private lateinit var confirmButton: Button
    private lateinit var nextButton: Button
    private lateinit var textViewTimer: TextView
    private lateinit var textNameError: TextView
    private lateinit var textPhoneNumberError: TextView
    private var isVerificationSent = false
    private var isVerificationConfirmed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_3)

        // Toolbar 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = getString(R.string.signup)

        editTextName = findViewById(R.id.editTextText)
        editTextPhone = findViewById(R.id.editTextText2)
        editTextVerification = findViewById(R.id.editTextText3)
        verifyButton = findViewById(R.id.verify_button)
        confirmButton = findViewById(R.id.confirm_button)
        nextButton = findViewById(R.id.button5)
        textViewTimer = findViewById(R.id.textView6)
        textNameError = findViewById(R.id.textName)
        textPhoneNumberError = findViewById(R.id.textPhoneNumber)

        editTextPhone.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deleteChar = false
            private var lastFormattedText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (count > 0 && after == 0) {
                    deleteChar = true
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isFormatting) {
                    formatPhoneNumber()
                }
            }

            override fun afterTextChanged(s: Editable?) {}

            private fun formatPhoneNumber() {
                isFormatting = true
                val text = editTextPhone.text.toString().replace("-", "")
                val length = text.length

                val formatted = when {
                    length > 6 -> text.substring(0, 3) + "-" + text.substring(3, 7) + "-" + text.substring(7)
                    length > 3 -> text.substring(0, 3) + "-" + text.substring(3)
                    else -> text
                }

                if (formatted != lastFormattedText) {
                    editTextPhone.setText(formatted)
                    editTextPhone.setSelection(formatted.length)
                    lastFormattedText = formatted
                }

                deleteChar = false
                isFormatting = false
            }
        })

        verifyButton.setOnClickListener {
            val phone = editTextPhone.text.toString().trim().replace("-", "")
            if (phone.matches(Regex("^\\d{11}\$"))) {
                isVerificationSent = true
                textNameError.visibility = View.GONE
                textPhoneNumberError.visibility = View.GONE
                startVerificationTimer()
                Toast.makeText(this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "올바른 전화번호 11자리를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                textPhoneNumberError.visibility = View.VISIBLE
            }
        }

        confirmButton.setOnClickListener {
            if (isVerificationSent) {
                val verificationCode = editTextVerification.text.toString().trim()
                if (verificationCode.isNotEmpty()) {
                    isVerificationConfirmed = true
                    Toast.makeText(this, "인증되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    textViewTimer.text = "인증번호를 입력해 주세요."
                }
            } else {
                textViewTimer.text = "인증 버튼 클릭 후 인증번호를 입력해 주세요."
            }
        }

        nextButton.setOnClickListener {
            if (validateInputs()) {
                val loginId = intent.getStringExtra("loginId") ?: ""
                val password = intent.getStringExtra("password") ?: ""
                val name = intent.getStringExtra("name") ?: ""
                val stature = intent.getFloatExtra("stature", 0f)
                val weight = intent.getFloatExtra("weight", 0f)
                val gender = intent.getStringExtra("gender") ?: ""
                val protectorName = editTextName.text.toString().trim()
                val protectorPhone = editTextPhone.text.toString().trim().replace("-", "")

                val newIntent = Intent(this, signUp4Activity::class.java).apply {
                    putExtra("loginId", loginId)
                    putExtra("password", password)
                    putExtra("name", name)
                    putExtra("stature", stature)
                    putExtra("weight", weight)
                    putExtra("gender", gender)
                    putExtra("protectorName", protectorName)
                    putExtra("protectorPhone", protectorPhone)
                }

                // 로그 메시지 출력
                Log.d("signUp3Activity", "전달 값 : loginId=$loginId, password=$password, name=$name, stature=$stature, weight=$weight, gender=$gender, protectorName=$protectorName, protectorPhone=$protectorPhone")

                startActivity(newIntent)
            } else {
                Toast.makeText(this, "모든 항목을 작성해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun validateInputs(): Boolean {
        val name = editTextName.text.toString().trim()
        val phone = editTextPhone.text.toString().trim().replace("-", "")
        val verification = editTextVerification.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || verification.isEmpty() || !isVerificationConfirmed) {
            if (name.isEmpty()) {
                textNameError.visibility = View.VISIBLE
            } else {
                textNameError.visibility = View.GONE
            }
            if (phone.isEmpty()) {
                textPhoneNumberError.visibility = View.VISIBLE
            } else {
                textPhoneNumberError.visibility = View.GONE
            }
            return false
        }
        return true
    }

    private fun startVerificationTimer() {
        val timer = object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                textViewTimer.text = String.format("인증번호* (%02d:%02d)", minutes, seconds)
            }

            override fun onFinish() {
                isVerificationSent = false
                textViewTimer.text = "인증 시간이 만료되었습니다."
            }
        }
        timer.start()
    }
}
