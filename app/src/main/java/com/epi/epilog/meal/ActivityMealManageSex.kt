package com.epi.epilog.meal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.R


class ActivityMealManageSex : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_manage_sex)

        // 버튼 찾기
        val nextButton = findViewById<Button>(R.id.meal_manage_sex_nextBtn)

        // 클릭 리스너 설정
        nextButton.setOnClickListener {
            // Intent를 사용하여 NextActivity로 전환
            val intent = Intent(this, ActivityMealManageExercise::class.java)
            startActivity(intent)
        }

    }
}
