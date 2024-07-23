package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityMealManageWeight : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_manage_weight)

        // 버튼 찾기
        val completeButton = findViewById<Button>(R.id.meal_manage_weight_Complete_Btn)

        completeButton.setOnClickListener {
            // Intent를 사용하여 NextActivity로 전환
            //val intent = Intent(this, ActivityMealManageExercise::class.java)
            //startActivity(intent)
        }


    }
}