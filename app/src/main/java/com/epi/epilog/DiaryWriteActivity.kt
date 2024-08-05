package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Locale

class DiaryWriteActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var saveButton: Button
    private lateinit var editButton: Button
    private lateinit var fragments: List<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_write)

        val selectedDate = intent.getStringExtra("date")
        val occurrenceType = intent.getStringExtra("occurrenceType")
        val selectedTime = intent.getStringExtra("time")

        Log.d("DiaryWriteActivity", "Intent data: date=$selectedDate, occurrenceType=$occurrenceType, time=$selectedTime")

        // 날짜 포맷팅
        val formattedDate = formatDateString(selectedDate)

        // 출력할 텍스트 생성
        val outputText = "$formattedDate ${occurrenceType ?: ""}"

        // TextView에 출력
        val textView: TextView = findViewById(R.id.diary_write_day)
        textView.text = outputText


        // Toolbar 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 기본 타이틀 숨기기
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Custom TextView 설정
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = getString(R.string.diary_edit_title)

        tabLayout = findViewById(R.id.tab_layout)
        saveButton = findViewById(R.id.save_button)
        editButton = findViewById(R.id.edit_button)
        fragments = listOf(
            DiaryFragmentBloodSugar(),
            DiaryFragmentBloodPressure(),
            DiaryFragmentWeight(),
            DiaryFragmentExercise(),
            DiaryFragmentMood(),
        )

        replaceFragment(fragments[0])

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

        //TODO : 수정버튼 눌렀을 시에 그냥 캘린더 화면으로 돌아가는 것도 나쁘지 않아보임.....
        editButton.setOnClickListener {
            val intent = Intent(this, DiaryEditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun formatDateString(dateString: String?): String {
        return try {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
            val date = originalFormat.parse(dateString)
            targetFormat.format(date)
        } catch (e: Exception) {
            dateString ?: ""
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
                else -> false.also { Log.d("FragmentCheck", "Unknown fragment type: $fragment") }
            }
            Log.d("FragmentCheck", "Fragment: $fragment, isFilledOut: $isFilledOut")
            isFilledOut
        }.also { Log.d("FragmentCheck", "isAnyFragmentFilledOut result: $it") }
    }
}
