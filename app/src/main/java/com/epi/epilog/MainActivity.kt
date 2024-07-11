package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.epi.epilog.databinding.ActivityMainNavBinding
import com.epi.epilog.databinding.ActivityModeSelectBinding
import com.epi.epilog.databinding.ActivitySeizureEditBinding
import com.epi.epilog.databinding.ActivityStartBinding
import com.epi.epilog.databinding.FragmentSeizureEdit9Binding
import com.epi.epilog.databinding.MainCalendarBinding
import com.epi.epilog.databinding.ActivityMainBinding
import com.epi.epilog.databinding.SignUp1Binding
import com.epi.epilog.databinding.SignUp2Binding
import com.epi.epilog.databinding.SignUp3Binding
import com.epi.epilog.databinding.SignUp4Binding
import com.epi.epilog.databinding.SignUp5Binding
import com.epi.epilog.databinding.MedicineAddModifyBinding
import com.epi.epilog.databinding.MedicineDetailBinding
import com.epi.epilog.databinding.MedicineChecklistBinding
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // StartActivity를 시작하고 현재 액티비티를 종료합니다.
        val intent = Intent(this, startActivity::class.java)
        startActivity(intent)
        finish()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewPager2와 Adapter 설정
        val viewPager = binding.calenderGraphViewpager
        val adapter = CalendarPagerAdapter(this)
        viewPager.adapter = adapter

        // TabLayout과 ViewPager2 연결
        val tabLayout = binding.tabs
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "캘린더"
                1 -> "그래프"
                else -> null
            }
        }.attach()

        // 기본 프래그먼트 설정
        if (savedInstanceState == null) {
            viewPager.currentItem = 0 // 첫 번째 페이지로 설정
        }
    }
}

class CalendarPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 2 // 총 프래그먼트 수
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CalendarFragment()
            1 -> graphFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }

    }
}

