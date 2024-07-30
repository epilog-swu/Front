package com.epi.epilog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.epi.epilog.databinding.ActivityMainBinding
import com.epi.epilog.meal.MealChecklistFragment
import com.ismaeldivita.chipnavigation.ChipNavigationBar


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainPageAdapter = MainPagerAdapter(this)

        setContentView(binding.root)

        with(binding) {
            viewPagerMain.adapter = mainPageAdapter

            mainLayoutBottomNavigation.setOnItemSelectedListener { id ->
                when (id) {
                    R.id.btm_menu_cal-> viewPagerMain.currentItem = 0
                    R.id.btm_menu_medicine-> viewPagerMain.currentItem = 1
                    R.id.btm_menu_food-> viewPagerMain.currentItem = 2
                    R.id.btm_menu_my-> viewPagerMain.currentItem = 3
                }
            }

            viewPagerMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    val id = when(position) {
                        0 -> R.id.btm_menu_cal
                        1 -> R.id.btm_menu_medicine
                        2 -> R.id.btm_menu_food
                        3 -> R.id.btm_menu_my
                        else -> return
                    }

                    mainLayoutBottomNavigation.setItemSelected(id)
                }
            })
        }

        // 기본 프래그먼트 설정
        if (savedInstanceState == null) {
            binding.viewPagerMain.currentItem = 0 // 첫 번째 페이지로 설정
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

class MainPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 4 // 총 프래그먼트 수
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CalendarFragment()
            1 -> MedicineChecklistFragment()  //TODO: medicine checklist fragment로 바꿔주세요!
            2 -> MealChecklistFragment()
            3 -> MealChecklistFragment () // TODO : Mypage fragment로 바꿔주세요!
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}



