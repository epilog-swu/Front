package com.epi.epilog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.databinding.ActivitySeizureEditBinding
import com.epi.epilog.databinding.FragmentSeizureEdit1Binding

// 발작일지 작성 액티비티
class SeizureEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySeizureEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction()
            .replace(R.id.seizure_edit_fragment, seizureEdit1Fragment()).commit()
    }
}