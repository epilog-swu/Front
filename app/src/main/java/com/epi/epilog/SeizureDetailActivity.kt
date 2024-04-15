package com.epi.epilog

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.databinding.ActivitySeizureDetailBinding

// 발작일지 상세보기 액티비티
class SeizureDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySeizureDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}