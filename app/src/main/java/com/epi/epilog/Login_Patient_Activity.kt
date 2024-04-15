package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.databinding.ActivityLoginPatientBinding


class Login_Patient_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPatientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //로그인 버튼이 눌려서 인증 성공 -> 메인 화면으로 전환
        binding.patientLoginBtn.setOnClickListener {
            val intent: Intent = Intent(this, MainCalendarActivity::class.java)
            startActivity(intent)
        }

    }
}