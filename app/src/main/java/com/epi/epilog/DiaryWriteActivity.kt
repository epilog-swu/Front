package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class DiaryWriteActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var saveButton: Button
    private lateinit var editButton: Button
    private lateinit var fragments: List<Fragment>
    private lateinit var diaryWriteday: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_write)

        // Initialize views
        tabLayout = findViewById(R.id.tab_layout)
        saveButton = findViewById(R.id.save_button)
        editButton = findViewById(R.id.edit_button)
        diaryWriteday = findViewById(R.id.diary_write_day)
        fragments = listOf(
            DiaryFragmentBloodSugar(),
            DiaryFragmentBloodPressure(),
            DiaryFragmentWeight(),
            DiaryFragmentExercise(),
            DiaryFragmentMood(),
            DiaryFragmentMedicine()
        )

        // Initialize the first tab with blood sugar fragment
        replaceFragment(fragments[0])

        // Set up the tab layout with a listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                replaceFragment(fragments[tab.position])
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        saveButton.setOnClickListener {
            if (isAnyFragmentFilledOut()) {
                val intent = Intent(this, ActivityShowSuccessDialog::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            } else {
                val intent = Intent(this, ActivityShowFailDialog::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }


        editButton.setOnClickListener {
            Toast.makeText(getApplicationContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show()
        }

        diaryWriteday.setOnClickListener{
            val intent = Intent(this, DiaryEditActivity::class.java)
            startActivity(intent)
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tab_content, fragment)
            .commit()
    }

    private fun isAnyFragmentFilledOut(): Boolean {
        return fragments.any { fragment ->
            val isFilledOut = when (fragment) {
                is DiaryFragmentBloodSugar -> fragment.isFilledOut().also { Log.d("FragmentCheck", "DiaryFragmentBloodSugar isFilledOut: $it") }
                is DiaryFragmentBloodPressure -> fragment.isFilledOut().also { Log.d("FragmentCheck", "DiaryFragmentBloodPressure isFilledOut: $it") }
                is DiaryFragmentWeight -> fragment.isFilledOut().also { Log.d("FragmentCheck", "DiaryFragmentWeight isFilledOut: $it") }
                is DiaryFragmentExercise -> fragment.isFilledOut().also { Log.d("FragmentCheck", "DiaryFragmentExercise isFilledOut: $it") }
                is DiaryFragmentMood -> fragment.isFilledOut().also { Log.d("FragmentCheck", "DiaryFragmentMood isFilledOut: $it") }
                is DiaryFragmentMedicine -> fragment.isFilledOut().also { Log.d("FragmentCheck", "DiaryFragmentMedicine isFilledOut: $it") }
                else -> false.also { Log.d("FragmentCheck", "Unknown fragment type: $fragment") }
            }
            Log.d("FragmentCheck", "Fragment: $fragment, isFilledOut: $isFilledOut")
            isFilledOut
        }.also { Log.d("FragmentCheck", "isAnyFragmentFilledOut result: $it") }
    }

}
