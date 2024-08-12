package com.epi.epilog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.epi.epilog.api.RetrofitClient.retrofitService
import com.epi.epilog.databinding.ActivityMainBinding
import com.epi.epilog.meal.MealChecklistFragment
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.epi.epilog.medicine.MedicineChecklistFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainPageAdapter = MainPagerAdapter(this)

        setContentView(binding.root)

        validateTokenAndNavigate()

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

        // 인텐트로부터 targetFragment가 전달된 경우 해당 프래그먼트로 이동
        intent?.getIntExtra("targetFragment", -1)?.let { targetFragment ->
            if (targetFragment != -1) {
                binding.viewPagerMain.currentItem = targetFragment
            }
        }
        // 텍스트 숨기기
        hideChipNavigationBarText(binding.mainLayoutBottomNavigation)
    }
    //토큰 유효성 검사
    private fun validateTokenAndNavigate() {
        val token = getTokenFromPreferences()
        if (token.isNullOrEmpty()) {
            redirectToLogin()
        } else {
            retrofitService.testApi("Bearer $token").enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) {
                        redirectToLogin()
                    } else {
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getTokenFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", null)
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
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
            1 -> MedicineChecklistFragment()
            2 -> MealChecklistFragment()
            3 -> MyPageFragment ()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}



