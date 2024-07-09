package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // StartActivity를 시작하고 현재 액티비티를 종료합니다.
        val intent = Intent(this, startActivity::class.java)
        startActivity(intent)
        finish()
    }
}


//
//class SeizureFragment: Fragment() {
//    lateinit var binding: FragmentSeizureEdit9Binding
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentSeizureEdit9Binding.inflate(inflater, container, false)
//        return binding.root
//    }
//}
//
//class SignUp1Activity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = SignUp1Binding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//    }
//}
//class SignUp2Activity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = SignUp2Binding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//    }
//}
//
//class SignUp3Activity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = SignUp3Binding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//    }
//}
//
//class SignUp4Activity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = SignUp4Binding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//    }
//}
//class SignUp5Activity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = SignUp5Binding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//    }
//}
//
//class MedicineAddModify : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = MedicineAddModifyBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//    }
//}
//class MedicineDetail : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = MedicineDetailBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//    }
//}
//class MedicineCheckList : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = MedicineChecklistBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, unable.newInstance())
//                .commitNow()
//        }
//        //복용지연알림 밑줄 코드
//        val tvDelayedMedication: TextView = findViewById(R.id.tvDelayedMedication) // TextView 참조
//        val content = "복용지연알림" // 밑줄을 적용할 텍스트
//        val spannableString = SpannableString(content)
//        spannableString.setSpan(UnderlineSpan(), 0, content.length, 0) // 전체 텍스트에 밑줄 적용
//        tvDelayedMedication.text = spannableString // TextView에 적용된 SpannableString 설정
//
//
//    }
//}
