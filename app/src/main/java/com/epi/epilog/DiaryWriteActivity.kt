package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class DiaryWriteActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var saveButton: Button
    private lateinit var editButton: Button
    private lateinit var fragments: List<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_write)

        // Initialize views
        tabLayout = findViewById(R.id.tab_layout)
        saveButton = findViewById(R.id.save_button)
        editButton = findViewById(R.id.edit_button)

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
                val intent = Intent(this, ActivityShowFailDialog::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            } else {
                val intent = Intent(this, ActivityShowSuccessDialog::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }

        editButton.setOnClickListener {
            Toast.makeText(getApplicationContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tab_content, fragment)
            .commit()
    }

    private fun isAnyFragmentFilledOut(): Boolean {
        return fragments.any { fragment ->
            when (fragment) {
                is DiaryFragmentBloodSugar -> fragment.isFilledOut()
                is DiaryFragmentBloodPressure -> fragment.isFilledOut()
                is DiaryFragmentWeight -> fragment.isFilledOut()
                is DiaryFragmentExercise -> fragment.isFilledOut()
                is DiaryFragmentMood -> fragment.isFilledOut()
                is DiaryFragmentMedicine -> fragment.isFilledOut()
                else -> false
            }
        }
    }
}
