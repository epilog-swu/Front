package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ModalBottomSheet
import androidx.fragment.app.DialogFragment
import com.epi.epilog.databinding.ActivitySeizureDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.epi.epilog.databinding.ActivityModeSelectBinding
import com.epi.epilog.databinding.ActivitySeizureEditBinding
import com.epi.epilog.databinding.ActivityStartBinding
import com.epi.epilog.databinding.FragmentSeizureEdit9Binding
import com.epi.epilog.databinding.SignUp1Binding
import com.epi.epilog.databinding.SignUp2Binding
import com.epi.epilog.databinding.SignUp3Binding
import com.epi.epilog.databinding.SignUp4Binding
import com.epi.epilog.databinding.SignUp5Binding
import com.epi.epilog.databinding.MedicineAddModifyBinding
import com.epi.epilog.databinding.MedicineDetailBinding
import com.epi.epilog.databinding.MedicineChecklistBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        super.onCreate(savedInstanceState)
//        val binding = ActivitySeizureEditBinding.inflate(layoutInflater)
//        setContentView(binding.root)

//        val fragmentManager: FragmentManager = supportFragmentManager
//        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
//        val seizureFragment = SeizureFragment()
//        transaction.add(R.id.seizure_edit_fragment, seizureFragment)
//        transaction.commit()
//        val intent = Intent(this, MedicineAddModify::class.java)
//        startActivity(intent)

    }
}

class SeizureFragment: Fragment() {
    lateinit var binding: FragmentSeizureEdit9Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSeizureEdit9Binding.inflate(inflater, container, false)
        return binding.root
    }
}

class SignUp1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp1Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
class SignUp2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp2Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}

class SignUp3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp3Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}

class SignUp4Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp4Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
class SignUp5Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp5Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}

class MedicineAddModify : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = MedicineAddModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
class MedicineDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = MedicineDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
class MedicineCheckList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = MedicineChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CalendarFragment.newInstance())
                .commitNow()
        }
        //복용지연알림 밑줄 코드
        val tvDelayedMedication: TextView = findViewById(R.id.tvDelayedMedication) // TextView 참조
        val content = "복용지연알림" // 밑줄을 적용할 텍스트
        val spannableString = SpannableString(content)
        spannableString.setSpan(UnderlineSpan(), 0, content.length, 0) // 전체 텍스트에 밑줄 적용
        tvDelayedMedication.text = spannableString // TextView에 적용된 SpannableString 설정


    }
}
