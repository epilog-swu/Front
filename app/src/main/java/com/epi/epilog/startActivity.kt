package com.epi.epilog

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
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.signup.signUp1Activity

class startActivity : AppCompatActivity() {

    private lateinit var logoFrame: FrameLayout
    private lateinit var logoImage: ImageView
    private lateinit var ovalImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        logoFrame = findViewById(R.id.logoFrame)
        logoImage = findViewById(R.id.logoImage)
        ovalImage = findViewById(R.id.ovalImage)
        val signUpButton: Button = findViewById(R.id.signUpButton)
        val nextButton: Button = findViewById(R.id.nextButton)

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
                navigateToMain() // 바로 메인 액티비티로 이동
            } else {
                redirectToLogin()
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
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