package com.epi.epilog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
<<<<<<< HEAD
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.epi.epilog.presentation.theme.api.TokenData
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
=======
>>>>>>> origin/main

class startActivity : AppCompatActivity() {

    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        initializeRetrofit()

        val signUpButton: Button = findViewById(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val intent = Intent(this, signUp1Activity::class.java)
            startActivity(intent)
        }

        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            // 유효성 검사 수행
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val authToken = sharedPreferences.getString("AuthToken", null)
            if (!authToken.isNullOrEmpty()) {
                validateToken(authToken)
            } else {
                // 토큰이 없으면 로그인 페이지로 이동
                redirectToLogin()
            }
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

    private fun validateToken(authToken: String) {
        retrofitService.testApi("Bearer $authToken").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 유효성 검사 성공 시 FCM 토큰 서버 전송
                    sendFcmToken(authToken)
                } else if (response.code() == 403) {
                    // 유효성 검사 실패 시 로그인 페이지로 이동
                    redirectToLogin()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 유효성 검사 실패 시 로그인 페이지로 이동
                redirectToLogin()
            }
        })
    }

    private fun sendFcmToken(authToken: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val fcmToken = task.result
            Log.d("FCM", "FCM Token: $fcmToken")

            val tokenData = TokenData(token = fcmToken)
            val call = retrofitService.postToken("Bearer $authToken", tokenData)
            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val tokenResponse = response.body()
                        if (tokenResponse?.success == true) {
                            Log.d("FCM", "Token saved successfully on the server: ${tokenResponse.message}")
                            // MainActivity로 이동
                            val intent = Intent(this@startActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Log.d("FCM", "Failed to save token on the server: ${tokenResponse?.message}")
                            // FCM 토큰 저장 실패 시 로그인 페이지로 이동
                            redirectToLogin()
                        }
                    } else {
                        Log.d("FCM", "Error saving token on the server: ${response.errorBody()?.string()}")
                        // FCM 토큰 저장 실패 시 로그인 페이지로 이동
                        redirectToLogin()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.d("startActivity", "Failed to send token to server: ${t.message}")
                    // FCM 토큰 저장 실패 시 로그인 페이지로 이동
                    redirectToLogin()
                }
            })
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
