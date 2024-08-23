package com.epi.epilog.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.MainActivity
import com.epi.epilog.R
import com.epi.epilog.api.Medication
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.SignUpRequest
import com.epi.epilog.api.SignUpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class signUp5Activity : AppCompatActivity() {

    private lateinit var completeButton: Button
    private lateinit var textViewCode: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_5)

        completeButton = findViewById(R.id.button5)
        textViewCode = findViewById(R.id.textViewCode)

        val textViewCopy = findViewById<TextView>(R.id.textViewCopy)
        val content = "갤럭시워치에서 연동해주세요"
        val spannableString = SpannableString(content)
        spannableString.setSpan(UnderlineSpan(), 0, content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textViewCopy.text = spannableString

        completeButton.setOnClickListener { signUp() }
    }

    private fun signUp() {
        val loginId = intent.getStringExtra("loginId") ?: return
        val password = intent.getStringExtra("password") ?: return
        val name = intent.getStringExtra("name") ?: return
        val stature = intent.getFloatExtra("stature", 0f)
        val weight = intent.getFloatExtra("weight", 0f)
        val gender = intent.getStringExtra("gender") ?: return
        val protectorName = intent.getStringExtra("protectorName") ?: return
        val protectorPhone = intent.getStringExtra("protectorPhone") ?: return
        val medications = intent.getSerializableExtra("medications") as? List<Medication> ?: emptyList()

        val signUpRequest = SignUpRequest(
            loginId = loginId,
            password = password,
            name = name,
            stature = stature,
            weight = weight,
            gender = gender,
            protectorName = protectorName,
            protectorPhone = protectorPhone,
            medication = medications
        )

        RetrofitClient.retrofitService.signUp(signUpRequest).enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        saveTokenToSession(responseBody.token)

                        Log.d("signUp5Activity", "전달 값 : loginId=$loginId, password=$password, name=$name, stature=$stature, weight=$weight, gender=$gender, protectorName=$protectorName, protectorPhone=$protectorPhone, medications=$medications")

                        navigateToMain()
                    } else {
                        Toast.makeText(this@signUp5Activity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@signUp5Activity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Toast.makeText(this@signUp5Activity, "회원가입 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveTokenToSession(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("AuthToken", token).apply()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
