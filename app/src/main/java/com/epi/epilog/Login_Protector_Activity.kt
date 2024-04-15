package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.databinding.ActivityLoginProtectorBinding

class Login_Protector_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginProtectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginProtectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //로그인 버튼이 눌려서 인증 성공 -> 메인 화면으로 전환
        binding.protectorLoginBtn.setOnClickListener {
            val intent: Intent = Intent(this, MainCalendarActivity::class.java)
            startActivity(intent)
        }
    }
}