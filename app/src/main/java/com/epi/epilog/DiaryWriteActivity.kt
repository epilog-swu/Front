package com.epi.epilog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import api.DiaryFragment
import com.epi.epilog.presentation.theme.api.DiaryRequest
import com.epi.epilog.presentation.theme.api.ExerciseEntry
import com.epi.epilog.presentation.theme.api.MoodEntry
import com.epi.epilog.presentation.theme.api.RetrofitService
import com.google.android.material.tabs.TabLayout
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class DiaryWriteActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var saveButton: Button
    private lateinit var editButton: Button
    private lateinit var fragments: List<Fragment>
    private lateinit var retrofitService: RetrofitService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_write)

        val selectedDate = intent.getStringExtra("date")
        val occurrenceType = intent.getStringExtra("occurrenceType")
        val selectedTime = intent.getStringExtra("time")
        val token = getTokenFromSession() //토큰 얻기

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
            DiaryFragmentBloodSugar.newInstance(selectedDate, occurrenceType, selectedTime),
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
                val intent = Intent(this,ActivityShowSuccessDialog::class.java)
                startActivity(intent)
                saveAllData()
            } else {
                val intent = Intent(this, ActivityShowFailDialog::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }

        editButton.setOnClickListener {
            val intent = Intent(this, DiaryEditActivity::class.java)
            startActivity(intent)
        }

        // 레트로핏 초기화
        initializeRetrofit()
    }

    private fun saveAllData() {
        val date = intent.getStringExtra("date") ?: ""
        val occurrenceType = intent.getStringExtra("occurrenceType") ?: ""

        val bloodSugar = getFragmentData<DiaryFragmentBloodSugar>()
        val bloodPressure = getFragmentData<DiaryFragmentBloodPressure>()
        val weight = getFragmentData<DiaryFragmentWeight>()
        val exercise = getFragmentData<DiaryFragmentExercise>()
        val mood = getFragmentData<DiaryFragmentMood>()

        val diaryRequest = DiaryRequest(
            date = date,
            occurrenceType = occurrenceType,
            bloodSugar = bloodSugar?.getString("bloodSugar"),
            systolicBloodPressure = bloodPressure?.getString("systolicBloodPressure"),
            diastolicBloodPressure = bloodPressure?.getString("diastolicBloodPressure"),
            heartRate = bloodPressure?.getString("heartRate"),
            weight = weight?.getString("weight"),
            bodyFatPercentage = weight?.getString("bodyFatPercentage"),
            bodyPhoto = weight?.getString("bodyPhoto"),
            exercise = exercise?.getJSONArray("exercise")?.let { parseExerciseEntries(it) } ?: emptyList(),
            mood = mood?.getJSONArray("mood")?.let { parseMoodEntries(it) } ?: emptyList()
        )

        // Retrofit을 사용하여 서버에 데이터 전송
        val token = getTokenFromSession()
        retrofitService.addDiary("Bearer $token", diaryRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        // 성공 처리
                        logResponseBody(response)
                        val intent = Intent(this@DiaryWriteActivity, ActivityShowSuccessDialog::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    } else {
                        // 실패 처리
                        handleFailure(apiResponse?.message ?: "Unknown error")
                    }
                } else {
                    handleFailure("Response code: ${response.code()}, message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                handleFailure(t.message ?: "Unknown error")
            }
        })
    }

    private fun parseExerciseEntries(array: JSONArray): List<ExerciseEntry> {
        val list = mutableListOf<ExerciseEntry>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            list.add(ExerciseEntry(item.getString("type"), item.optString("details", null)))
        }
        return list
    }

    private fun parseMoodEntries(array: JSONArray): List<MoodEntry> {
        val list = mutableListOf<MoodEntry>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            list.add(MoodEntry(item.getString("type"), item.optString("details", null)))
        }
        return list
    }

    private inline fun <reified T : DiaryFragment> getFragmentData(): JSONObject? {
        return fragments.find { it is T }?.let { (it as T).getData() }
    }


    private fun initializeRetrofit() {
        val gson = com.google.gson.GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    private fun getTokenFromSession(): String {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("AuthToken", "")
        Log.d("Token", "AuthToken: $token")
        return token ?: ""
    }

    // 두 JSON 객체를 병합하는 함수
    private fun mergeJsonObjects(target: JSONObject, source: JSONObject) {
        val keys = source.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            target.put(key, source.get(key))
        }
    }

    private fun handleFailure(errorMessage: String) {
        // 로그 기록
        Log.e("SaveAllDataError", errorMessage)

        // 사용자에게 실패 메시지 표시
        runOnUiThread {
            Toast.makeText(this, "데이터 저장 실패: $errorMessage", Toast.LENGTH_LONG).show()
        }
    }

    private fun logResponseBody(response: Response<ApiResponse>) {
        // 응답 본문 로그 출력
        Log.d("ResponseBody", "Response Body: ${response.body()}")
    }

    //JSON 데이터 확인
    private fun logFinalData(finalData: JSONObject) {
        Log.d("FinalData", "Final JSON Data: $finalData")
    }

    //상단바 날짜 바꾸기
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
