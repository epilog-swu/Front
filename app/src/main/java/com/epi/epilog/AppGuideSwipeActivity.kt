package com.epi.epilog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.epi.epilog.databinding.ActivityAppGuideSwipeBinding


class AppGuideSwipeActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAppGuideSwipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppGuideSwipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.appGuideViewpager // ViewPager2를 참조합니다.
        val indicator = binding.appGuideIndicator // 인디케이터를 참조합니다.


        // Fragment를 ViewPager2에 연결하는 어댑터를 생성합니다.
        val appGuideSwipeAdapter = AppGuideSwipeAdapter(this)
        appGuideSwipeAdapter.addFragment(AppGuide1Fragment())
        appGuideSwipeAdapter.addFragment(AppGuide2Fragment())
        appGuideSwipeAdapter.addFragment(AppGuide3Fragment())
        // 필요한 만큼의 Fragment를 추가하세요.

        // ViewPager2에 어댑터를 설정합니다.
        binding.appGuideViewpager.adapter = appGuideSwipeAdapter

        // ViewPager2의 방향을 수평으로 설정합니다.
        binding.appGuideViewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // ViewPager2의 현재 페이지를 설정하여 appGuide1Fragment를 기본으로 보여줍니다.
        binding.appGuideViewpager.setCurrentItem(0, false) // 첫 번째 페이지를 선택합니다. (인덱스는 0부터 시작합니다.)

        // ViewPager2와 인디케이터를 연결합니다.
        indicator.setViewPager(viewPager)

    }
}