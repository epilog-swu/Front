package com.epi.epilog

import android.content.Context
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import com.epi.epilog.api.RetrofitService
import com.epi.epilog.signup.signUp1Activity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class startActivity : AppCompatActivity() {

    private lateinit var retrofitService: RetrofitService
    private lateinit var logoFrame: FrameLayout
    private lateinit var logoImage: ImageView
    private lateinit var ovalImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        initializeRetrofit()

        logoFrame = findViewById(R.id.logoFrame)
        logoImage = findViewById(R.id.logoImage)
        ovalImage = findViewById(R.id.ovalImage)
        val signUpButton: Button = findViewById(R.id.signUpButton)
        val nextButton: Button = findViewById(R.id.nextButton)

        // FrameLayout을 먼저 보여줍니다.
        logoFrame.visibility = View.VISIBLE

        // 배경색을 서서히 변경합니다.
        val background = findViewById<View>(R.id.startLayout).background
        if (background is TransitionDrawable) {
            background.startTransition(6000) // 배경색 전환 시간
        }

        logoImage.postDelayed({
            ovalImage.visibility = View.VISIBLE
            logoImage.updateLayoutParams<FrameLayout.LayoutParams> {
                updateMargins(right = dpToPx(15))
            }
        }, 2000)

        // 1초 뒤에 버튼들을 표시합니다.
        logoFrame.postDelayed({
            findViewById<View>(R.id.buttonsLayout).visibility = View.VISIBLE
        }, 3000)

        signUpButton.setOnClickListener {
            val intent = Intent(this, signUp1Activity::class.java)
            startActivity(intent)
        }

        nextButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val authToken = sharedPreferences.getString("AuthToken", null)
            if (!authToken.isNullOrEmpty()) {
                validateToken(authToken)
            } else {
                redirectToLogin()
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
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
                    sendFCMTokenThenRedirect()
                } else {
                    redirectToLogin()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                redirectToLogin()
            }
        })
    }

    private fun sendFCMTokenThenRedirect() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                // Send this token to your server
                Log.d("FCM Token", fcmToken)
            }
            // After sending FCM Token, redirect to MainActivity
            redirectToMain()
        }
    }

    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
