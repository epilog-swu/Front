package com.epi.epilog.diary

import android.app.Activity
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
import com.epi.epilog.api.ApiResponse
import com.epi.epilog.main.MainActivity
import com.epi.epilog.R
import com.epi.epilog.api.DiaryRequest
import com.epi.epilog.api.ExerciseEntry
import com.epi.epilog.api.MoodEntry
import com.epi.epilog.api.RetrofitService
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
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
                val intent = Intent(this, ActivityShowSuccessDialog::class.java)
                startActivityForResult(intent, REQUEST_CODE_SUCCESS_DIALOG)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SUCCESS_DIALOG) {
            if (resultCode == Activity.RESULT_OK) {
                saveAllData()

                // 홈 화면으로 이동
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("targetFragment", 0)  // 0은 CalendarFragment의 인덱스
                startActivity(intent)
                finish()
            }
        }
    }

    companion object {
        const val REQUEST_CODE_SUCCESS_DIALOG = 1
    }

    private fun saveAllData() {
        val date = intent.getStringExtra("date") ?: ""
        val occurrenceType = intent.getStringExtra("occurrenceType") ?: ""

        Log.d("saveAllData", "Date: $date, OccurrenceType: $occurrenceType")

        // 각 프래그먼트에서 데이터를 가져오되, 초기화되지 않은 경우 null로 설정
        val bloodSugar = getFragmentData<DiaryFragmentBloodSugar>()?.optString("bloodSugar") ?: null
        val systolicBloodPressure = getFragmentData<DiaryFragmentBloodPressure>()?.optString("systolicBloodPressure") ?: null
        val diastolicBloodPressure = getFragmentData<DiaryFragmentBloodPressure>()?.optString("diastolicBloodPressure") ?: null
        val heartRate = getFragmentData<DiaryFragmentBloodPressure>()?.optString("heartRate") ?: null
        val weight = getFragmentData<DiaryFragmentWeight>()?.optString("weight") ?: null
        val bodyFatPercentage = getFragmentData<DiaryFragmentWeight>()?.optString("bodyFatPercentage") ?: null
        val bodyPhoto = getFragmentData<DiaryFragmentWeight>()?.let { fragmentData ->
            if (fragmentData.isNull("bodyPhoto")) null else fragmentData.getString("bodyPhoto")
        }
        val exercise = getFragmentData<DiaryFragmentExercise>()?.optJSONArray("exercise")?.let { parseExerciseEntries(it) } ?: null
        val mood = getFragmentData<DiaryFragmentMood>()?.optJSONArray("mood")?.let { parseMoodEntries(it) } ?: null

        val diaryRequest = DiaryRequest(
            date = date,
            occurenceType = occurrenceType,
            bloodSugar = bloodSugar,
            systolicBloodPressure = systolicBloodPressure,
            diastolicBloodPressure = diastolicBloodPressure,
            heartRate = heartRate,
            weight = weight,
            bodyFatPercentage = bodyFatPercentage,
            bodyPhoto = bodyPhoto,
            exercise = exercise,
            mood = mood
        )

        val gson = GsonBuilder().serializeNulls().create()
        val requestDataJson = gson.toJson(diaryRequest)

        Log.d("RequestData", "Sending request data: $requestDataJson")

        val token = getTokenFromSession()
        retrofitService.addDiary("Bearer $token", diaryRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        logResponseBody(response)
                        overridePendingTransition(0, 0)
                    } else {
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


    // Exercise 데이터 수집 시 조건에 따라 항목을 필터링
    private fun parseExerciseEntries(array: JSONArray): List<ExerciseEntry> {
        val list = mutableListOf<ExerciseEntry>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val type = item.getString("type").replace("직접 입력", "직접입력")
            val details = item.optString("details").takeIf { it != "null" && it.isNotEmpty() }

            // "직접 입력" 타입일 경우, details가 있을 때만 추가
            if (type != "직접입력" || details != null) {
                list.add(ExerciseEntry(type, details))
            }
        }
        return list
    }

    private fun parseMoodEntries(array: JSONArray): List<MoodEntry> {
        val list = mutableListOf<MoodEntry>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val type = item.getString("type").replace("직접 입력", "직접입력") // 여기서 변경
            val details = item.optString("details").takeIf { it != "null" && it.isNotEmpty() }

            // "직접 입력" 타입일 경우, details가 있을 때만 추가
            if (type != "직접입력" || details != null) {
                list.add(MoodEntry(type, details))
            }
        }
        return list

    }




    private inline fun <reified T : DiaryFragment> getFragmentData(): JSONObject? {
        return try {
            fragments.find { it is T }?.let { (it as T).getData() }
        } catch (e: UninitializedPropertyAccessException) {
            null
        }
    }


    private fun initializeRetrofit() {
        val gson = GsonBuilder().serializeNulls().setLenient().create()
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
