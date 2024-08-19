package com.epi.epilog

import android.content.Context
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
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

        // FrameLayout을 먼저 보여줍니다.
        logoFrame.visibility = View.VISIBLE

        // 배경색을 서서히 변경합니다.
        val background = findViewById<View>(R.id.startLayout).background
        if (background is TransitionDrawable) {
            background.startTransition(5000) // 배경색 전환 시간
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
