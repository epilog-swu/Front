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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_write)

//        // Initialize views
//        tabLayout = findViewById(R.id.tab_layout)
//        saveButton = findViewById(R.id.save_button)
//        editButton = findViewById(R.id.edit_button)
//
//        // Initialize the first tab with blood sugar fragment
//        replaceFragment(DiaryFragmentBloodSugar())
//
//        // Set up the tab layout with a listener
//        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                when (tab.position) {
//                    0 -> replaceFragment(DiaryFragmentBloodSugar())
//                    1 -> replaceFragment(DiaryFragmentBloodPressure())
//                    2 -> replaceFragment(DiaryFragmentWeight())
//                    3 -> replaceFragment(DiaryFragmentExercise())
//                    4 -> replaceFragment(DiaryFragmentMood())
//                    5 -> replaceFragment(DiaryFragmentMedicine())
//                }
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab) {}
//
//            override fun onTabReselected(tab: TabLayout.Tab) {}
//        })
//
//        saveButton.setOnClickListener {
//            // TODO: Implement save logic and validation here
//            if (validateInputs()) {
//                // Save the data and show success message
//                Toast.makeText(this, "일지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
//            } else {
//                // Show error message
//                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        editButton.setOnClickListener {
//            val intent = Intent(this, DiaryEditActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//    private fun replaceFragment(fragment: Fragment) {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.tab_content, fragment)
//            .commit()
//    }
//
//    private fun validateInputs(): Boolean {
//        // Implement input validation logic here
//        return true
//    }
}}
