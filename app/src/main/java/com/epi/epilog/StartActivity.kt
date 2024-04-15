package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.databinding.ActivityStartBinding
import com.epi.epilog.databinding.SignUp1Binding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //현아가 SignUp1Activity 만들면 인텐트 연결
//        binding.startLayoutSignupBtn.setOnClickListener {
//            val intent:Intent = Intent(this, SignUp1Activity::class.java)
//            startActivity(intent)
//            }

        binding.startLayoutNextBtn.setOnClickListener {
            val intent:Intent = Intent(this, ModeSelectActivity::class.java)
            startActivity(intent)
        }
    }

}

