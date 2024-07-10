package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
            showAppropriateDialog()
        }

        editButton.setOnClickListener {
            val intent = Intent(this, DiaryEditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tab_content, fragment)
            .commit()
    }

    private fun showAppropriateDialog() {
        if (isAnyFragmentFilledOut()) {
            showCustomDialog(R.layout.dialog_success)
        } else {
            showCustomDialog(R.layout.dialog_fail)
        }
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

    private fun showCustomDialog(layoutId: Int) {
        MaterialAlertDialogBuilder(this)
            .setView(layoutId)
            .show()
    }
}
