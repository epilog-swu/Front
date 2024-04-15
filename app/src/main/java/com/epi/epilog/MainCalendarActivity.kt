package com.epi.epilog


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.epi.epilog.AppGuide1Fragment
import com.epi.epilog.AppGuide2Fragment
import com.epi.epilog.GraphFragment
import com.epi.epilog.MainCalendarFragment
import com.epi.epilog.databinding.MainCalendarBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainCalendarActivity : AppCompatActivity() {

    private lateinit var binding: MainCalendarBinding // 바인딩 변수 선언

    private val information = arrayListOf("캘린더", "그래프")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainCalendarBinding.inflate(layoutInflater) // 바인딩 초기화
        setContentView(binding.root) // 바인딩된 레이아웃을 화면에 설정

        // ViewPager 어댑터 설정
        val MainCalendarAdapter = MainCalendarAdapter(this)
        MainCalendarAdapter.addFragment(MainCalendarFragment())
        MainCalendarAdapter.addFragment(GraphFragment())


        // ViewPager2에 어댑터를 설정합니다.
        binding.calenderGraphViewpager.adapter = MainCalendarAdapter

        // ViewPager2의 방향을 수평으로 설정합니다.
        binding.calenderGraphViewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL


        // ViewPager2의 현재 페이지를 설정하여 캘린더를 기본으로 보여줍니다.
        binding.calenderGraphViewpager.setCurrentItem(0, false) // 첫 번째 페이지를 선택합니다. (인덱스는 0부터 시작합니다.)


//        // TabLayout과 ViewPager2 연결
//        TabLayoutMediator(binding.tabs, binding.calenderGraphViewpager) { tab, position ->
//            tab.text = MainCalendarAdapter.getPageTitle(position)
//        }.attach()
//
//        // 탭 선택 리스너 설정 (선택 시 ViewPager2 변경)
//        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                binding.calenderGraphViewpager.currentItem = tab.position
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {}
//            override fun onTabReselected(tab: TabLayout.Tab?) {}
//        })
    }

    }

