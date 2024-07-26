package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.presentation.theme.api.RetrofitService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class startActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)



        val signUpButton: Button = findViewById(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val intent = Intent(this, signUp1Activity::class.java)
            startActivity(intent)
        }

        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
