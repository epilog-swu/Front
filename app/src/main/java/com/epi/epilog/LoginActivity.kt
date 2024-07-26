package com.epi.epilog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.presentation.theme.api.LoginRequest
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_patient)


        initializeRetrofit()
        val preButton: Button = findViewById(R.id.pre_button)
        val loginButton: Button = findViewById(R.id.login_button)
        val loginIdEditText: EditText = findViewById(R.id.id_edit)
        val passwordEditText: EditText = findViewById(R.id.password_edit)
        val signUpText: TextView = findViewById(R.id.signup_text)

        // 기본적으로 비밀번호 숨김 설정
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // 비밀번호 보이기/숨기기 기능 추가
        passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = 2
                if (event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[drawableRight].bounds.width())) {
                    togglePasswordVisibility(passwordEditText)
                    return@setOnTouchListener true
                }
            }
            false
        }
        preButton.setOnClickListener{
            val intent = Intent(this, startActivity::class.java)
            startActivity(intent)
        }
        // 회원가입 텍스트에 밑줄 적용
        val signUpTextString = "계정이 없으신가요? 회원가입하기"
        val spannableString = SpannableString(signUpTextString)
        val startIndex = signUpTextString.indexOf("회원가입하기")
        val endIndex = startIndex + "회원가입하기".length
        spannableString.setSpan(UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        signUpText.text = spannableString

        loginButton.setOnClickListener {
            val loginId = loginIdEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (loginId.isNotEmpty() && password.isNotEmpty()) {
                login(LoginRequest(loginId, password))
            } else {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        signUpText.setOnClickListener {
            val intent = Intent(this, signUp1Activity::class.java)
            startActivity(intent)
        }
    }


    private fun initializeRetrofit() {
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun login(request: LoginRequest) {
        retrofitService.login(request).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    saveTokenToSession(responseBody)
                    Log.d("LoginActivity", "Token: $responseBody")
                    // 로그인 성공 시 MainActivity로 이동
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "회원가입을 먼저 해주세요", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "로그인 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveTokenToSession(token: String?) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("AuthToken", token).apply()
    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text.length)
    }
}
