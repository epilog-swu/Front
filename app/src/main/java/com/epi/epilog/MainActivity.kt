package com.epi.epilog

import android.R
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.epi.epilog.databinding.ActivityMainBinding
import com.ismaeldivita.chipnavigation.ChipNavigationBar


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // StartActivity를 시작하고 현재 액티비티를 종료합니다.
//        val intent = Intent(this, startActivity::class.java)
//        startActivity(intent)
//        finish()

        setContentView(binding.root)

        with(binding) {
            val buttonBackgroundColor = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
                intArrayOf(Color.WHITE, Color.TRANSPARENT)
            )

            calendarButton.setCardBackgroundColor(buttonBackgroundColor)
            graphButton.setCardBackgroundColor(buttonBackgroundColor)

            calendarButton.setOnClickListener { calenderGraphViewpager.currentItem = 0 }
            graphButton.setOnClickListener { calenderGraphViewpager.currentItem = 1 }

            val adapter = CalendarPagerAdapter(this@MainActivity)
            with(calenderGraphViewpager) {
                this.adapter = adapter
                this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        if (position == 0) {
                            calendarButton.isSelected = true
                            graphButton.isSelected = false
                        } else {
                            calendarButton.isSelected = false
                            graphButton.isSelected = true
                        }
                    }
                })
            }

            calenderGraphViewpager.adapter = adapter

        }

        // 기본 프래그먼트 설정
        if (savedInstanceState == null) {
            binding.calenderGraphViewpager.currentItem = 0 // 첫 번째 페이지로 설정
        }
        // 텍스트 숨기기
        hideChipNavigationBarText(binding.mainLayoutBottomNavigation)
    }

    private fun hideChipNavigationBarText(chipNavigationBar: ChipNavigationBar) {
        val menuView = chipNavigationBar.getChildAt(0) as ViewGroup
        for (i in 0 until menuView.childCount) {
            val itemView = menuView.getChildAt(i) as ViewGroup
            for (j in 0 until itemView.childCount) {
                val innerView = itemView.getChildAt(j)
                if (innerView is TextView) {
                    innerView.visibility = View.GONE  // 텍스트 뷰 숨기기
                }
            }
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

