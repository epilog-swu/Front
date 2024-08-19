package com.epi.epilog.medicine


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.epi.epilog.MainActivity
import com.epi.epilog.R
import com.epi.epilog.api.MedicationDetailResponse
import com.epi.epilog.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MedicineDetailActivity : AppCompatActivity() {
    private lateinit var medicineNameTextView: TextView
    private lateinit var medicineEffectTextView: TextView
    private lateinit var medicinePrecautionTextView: TextView
    private lateinit var medicineStorageTextView: TextView
    private lateinit var medicinePeriodStartTextView: TextView
    private lateinit var medicinePeriodEndTextView: TextView
    private lateinit var medicineTimesLayout: LinearLayout
    private lateinit var medicineDaysTextView: TextView
    private lateinit var alarmStatusTextView: TextView
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var deleteButton: Button
    private lateinit var adjustButton: Button // 추가된 버튼 참조
    private lateinit var token: String
    private var currentMedicationId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.medicine_detail)

        // 뷰 초기화
        medicineNameTextView = findViewById(R.id.medicine_name)
        medicineEffectTextView = findViewById(R.id.medicine_effect)
        medicinePrecautionTextView = findViewById(R.id.medicine_precaution)
        medicineStorageTextView = findViewById(R.id.medicine_storage)
        medicinePeriodStartTextView = findViewById(R.id.medicine_period_start)
        medicinePeriodEndTextView = findViewById(R.id.medicine_period_end)
        medicineTimesLayout = findViewById(R.id.medicine_times_layout)
        medicineDaysTextView = findViewById(R.id.medicine_days)
        alarmStatusTextView = findViewById(R.id.alarm_status)
        nextButton = findViewById(R.id.next_button)
        prevButton = findViewById(R.id.prev_button)
        deleteButton = findViewById(R.id.delete_button)
        adjustButton = findViewById(R.id.adjust_button) // 추가된 버튼 초기화

        token = getTokenFromSession()

        // Intent로부터 medicationId를 가져와서 currentMedicationId에 설정
        currentMedicationId = intent.getIntExtra("medicationId", -1)

        // 처음에는 currentMedicationId가 유효한 경우에만 fetch
        if (currentMedicationId != null && currentMedicationId != -1) {
            fetchMedicationDetail(currentMedicationId!!)
        } else {
            Toast.makeText(this, "Invalid Medication ID", Toast.LENGTH_SHORT).show()
        }

        prevButton.setOnClickListener {
            val prevId = intent.getIntExtra("prevId", -1)
            if (prevId != -1) {
                fetchMedicationDetail(prevId)
            } else {
                Toast.makeText(this, "No previous medication available", Toast.LENGTH_SHORT).show()
            }
        }

        nextButton.setOnClickListener {
            val nextId = intent.getIntExtra("nextId", -1)
            if (nextId != -1) {
                fetchMedicationDetail(nextId)
            } else {
                Toast.makeText(this, "No next medication available", Toast.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            val prevId = intent.getIntExtra("prevId", -1)
            val nextId = intent.getIntExtra("nextId", -1)
            Log.d("MedicineDetailActivity", "Deleting Medication ID: $currentMedicationId, Next ID: $nextId, Previous ID: $prevId")

            val deleteDialogFragment = DeleteDialogFragment.newInstance(
                currentMedicationId,
                nextId,
                prevId
            )
            deleteDialogFragment.show(supportFragmentManager, "deleteDialog")
        }

        // adjust_button 클릭 시
        adjustButton.setOnClickListener {
            Toast.makeText(this, "약이 수정되었습니다.", Toast.LENGTH_SHORT).show()
            navigateToMainActivity() // MainActivity로 이동
        }
    }

    private fun fetchMedicationDetail(medicationId: Int) {
        Log.d("fetchMedicationDetail", "Fetching details for Medication ID: $medicationId with token: $token")
        RetrofitClient.retrofitService.getMedicationDetail(medicationId, "Bearer $token")
            .enqueue(object : Callback<MedicationDetailResponse> {
                override fun onResponse(
                    call: Call<MedicationDetailResponse>,
                    response: Response<MedicationDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            updateUI(it)
                            currentMedicationId = medicationId
                        }
                    } else {
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string()
                        Log.e("fetchMedicationDetail", "Failed to load details. Error code: $errorCode, Error body: $errorBody")
                        Toast.makeText(
                            this@MedicineDetailActivity,
                            "Failed to load details. Error code: $errorCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MedicationDetailResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MedicineDetailActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun updateMedicationId(newId: Int?) {
        newId?.let {
            fetchMedicationDetail(it)
        } ?: run {
            Toast.makeText(this, "No more medications available", Toast.LENGTH_SHORT).show()
            finish()  // 더 이상 약물이 없으면 액티비티 종료
        }
    }

    private fun updateUI(details: MedicationDetailResponse) {
        Log.d("MedicationDetail", "startDate: ${details.startDate}, endDate: ${details.endDate}")
        val formattedStartDate = formatDateString(details.startDate)
        val formattedEndDate = formatDateString(details.endDate)
        medicineNameTextView.text = details.medicationName
        medicineEffectTextView.text = details.effectiveness
        medicinePrecautionTextView.text = details.precautions
        medicineStorageTextView.text = details.storageMethod
        medicinePeriodStartTextView.text = formattedStartDate
        medicinePeriodEndTextView.text = formattedEndDate

        medicineTimesLayout.removeAllViews()

        for (time in details.times) {
            val timeTextView = TextView(this).apply {
                text = time
                setBackgroundResource(R.drawable.badge)
                setTextColor(ContextCompat.getColor(this@MedicineDetailActivity, R.color.black))
                textSize = 16f
                setPadding(16, 6, 16, 6)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 17, 0)
                layoutParams = params
            }

            medicineTimesLayout.addView(timeTextView)
        }

        medicineDaysTextView.text = details.weeks.joinToString(", ")
        alarmStatusTextView.text = if (details.isAlarm) "복용 알람이 켜져있습니다" else "복용 알람이 꺼져있습니다"

        prevButton.isEnabled = details.prevId != 0
        nextButton.isEnabled = details.nextId != null

        prevButton.setOnClickListener {
            details.prevId?.let {
                currentMedicationId = it
                fetchMedicationDetail(it)
            }
        }

        nextButton.setOnClickListener {
            details.nextId?.let {
                currentMedicationId = it
                fetchMedicationDetail(it)
            }
        }
    }

    private fun getTokenFromSession(): String {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AuthToken", "") ?: ""
    }

    private fun formatDateString(date: String): String {
        val parts = date.split(" ")
        return if (parts.size == 3) {
            "${parts[0]}년 ${parts[1]}월 ${parts[2]}일"
        } else {
            date
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
