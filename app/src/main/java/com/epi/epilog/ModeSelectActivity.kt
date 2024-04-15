package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.databinding.ActivityModeSelectBinding


class ModeSelectActivity: AppCompatActivity() {

    private lateinit var binding: ActivityModeSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.modePatientBtn.setOnClickListener {
            val intent: Intent = Intent(this, Login_Patient_Activity::class.java)
            startActivity(intent)
        }

        binding.modeProtectorBtn.setOnClickListener {
            val intent: Intent = Intent(this, Login_Protector_Activity::class.java)
            startActivity(intent)
        }

    }
}