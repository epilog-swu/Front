package com.epi.epilog.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.R
import com.epi.epilog.api.RetrofitService
import com.epi.epilog.main.MainActivity
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StartActivity : AppCompatActivity() {

    private lateinit var logoFrame: FrameLayout
    private lateinit var logoImage: ImageView
    private lateinit var ovalImage: ImageView
    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        logoFrame = findViewById(R.id.logoFrame)
        logoImage = findViewById(R.id.logoImage)
        ovalImage = findViewById(R.id.ovalImage)
        val signUpButton: Button = findViewById(R.id.signUpButton)
        val nextButton: Button = findViewById(R.id.nextButton)

        initializeRetrofit()

        // 앱 시작 시 토큰 유효성 검사
        validateTokenAndProceed()

        logoFrame.visibility = View.VISIBLE

        val background = findViewById<View>(R.id.startLayout).background
        if (background is TransitionDrawable) {
            background.startTransition(3000) // 배경색 전환 시간
        }

        logoFrame.postDelayed({
            ovalImage.visibility = View.VISIBLE

            // 애니메이션 설정
            val ovalMoveLeft = ObjectAnimator.ofFloat(ovalImage, "translationX", dpToPx(-10).toFloat())
            val logoMoveLeft = ObjectAnimator.ofFloat(logoImage, "translationX", dpToPx(-15).toFloat())

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(ovalMoveLeft, logoMoveLeft)
            animatorSet.duration = 700 // 애니메이션 지속 시간
            animatorSet.start()
        }, 1100)

        logoFrame.postDelayed({
            findViewById<View>(R.id.buttonsLayout).visibility = View.VISIBLE
        }, 1900)

        signUpButton.setOnClickListener {
            val intent = Intent(this, signUp1Activity::class.java)
            startActivity(intent)
        }

        nextButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val authToken = sharedPreferences.getString("AuthToken", null)
            if (!authToken.isNullOrEmpty()) {
                navigateToMain()
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

    private fun validateTokenAndProceed() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("AuthToken", null)

        if (authToken.isNullOrEmpty()) {
            // 토큰이 없는 경우 그냥 StartActivity에 머무름
            return
        }

        retrofitService.testApi("Bearer $authToken").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 토큰이 유효하면 메인 액티비티로 이동
                    navigateToMain()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@StartActivity, "토큰 유효성 검사 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToMain() {
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
