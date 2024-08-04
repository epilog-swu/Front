package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.epi.epilog.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class signUp1Activity : AppCompatActivity() {

    private lateinit var editTextText: EditText
    private lateinit var editTextText2: EditText
    private lateinit var editTextText3: EditText
    private lateinit var button5: Button
    private lateinit var idErrorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_1)

        // Toolbar 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = getString(R.string.signup)

        editTextText = findViewById(R.id.editTextText)
        editTextText2 = findViewById(R.id.editTextText2)
        editTextText3 = findViewById(R.id.editTextText3)
        button5 = findViewById(R.id.button5)
        idErrorTextView = findViewById(R.id.idErrorTextView)

        // 비밀번호를 기본적으로 보이지 않게 설정
        editTextText2.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        editTextText3.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        button5.setOnClickListener {
            val validationResult = validateInputs()
            if (validationResult == "success") {
                checkIdAndProceed()
            } else {
                Toast.makeText(this, validationResult, Toast.LENGTH_SHORT).show()
            }
        }

        editTextText2.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = 2
                if (event.rawX >= (editTextText2.right - editTextText2.compoundDrawables[drawableRight].bounds.width())) {
                    togglePasswordVisibility(editTextText2)
                    return@setOnTouchListener true
                }
            }
            false
        }

        editTextText3.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = 2
                if (event.rawX >= (editTextText3.right - editTextText3.compoundDrawables[drawableRight].bounds.width())) {
                    togglePasswordVisibility(editTextText3)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun validateInputs(): String {
        val userId = editTextText.text.toString().trim()
        val password = editTextText2.text.toString().trim()
        val confirmPassword = editTextText3.text.toString().trim()

        return when {
            userId.isEmpty() -> "모든 항목을 작성해주세요"
            password.isEmpty() -> "모든 항목을 작성해주세요"
            confirmPassword.isEmpty() -> "모든 항목을 작성해주세요"
            password != confirmPassword -> "비밀번호가 일치하지 않습니다"
            else -> "success"
        }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text.length)
    }

    private fun checkIdAndProceed() {
        val userId = editTextText.text.toString().trim()
        RetrofitClient.retrofitService.validateId(userId).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("API_RESPONSE", "Response body: $body")

                    if (body?.success == true) {
                        proceedToNextStep()
                    } else {
                        idErrorTextView.visibility = View.VISIBLE
                        Toast.makeText(
                            this@signUp1Activity,
                            body?.message ?: "아이디 중복 확인 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@signUp1Activity, "아이디 중복 확인 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(
                    this@signUp1Activity,
                    "아이디 중복 확인 실패: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun proceedToNextStep() {
        val loginId = editTextText.text.toString().trim()
        val password = editTextText2.text.toString().trim()

        val intent = Intent(this, signUp2Activity::class.java).apply {
            putExtra("loginId", loginId)
            putExtra("password", password)
        }

        startActivity(intent)
    }
}
