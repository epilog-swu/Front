package com.epi.epilog.medicine

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.epi.epilog.ApiResponse
import com.epi.epilog.MainActivity
import com.epi.epilog.R
import com.epi.epilog.api.Medication
import com.epi.epilog.api.MedicationRequest
import com.epi.epilog.api.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MedicineAddModifyActivity : AppCompatActivity() {

    private lateinit var medicationEditText: EditText
    private lateinit var addButton: Button
    private lateinit var medicationList: LinearLayout
    private lateinit var saveButton: Button
    private lateinit var startDateText: TextView
    private lateinit var endDateText: TextView
    private lateinit var checkBoxNoEndDate: CheckBox
    private lateinit var switchAlarm: Switch
    private lateinit var effectivenessEditText: EditText
    private lateinit var precautionsEditText: EditText
    private lateinit var storageMethodEditText: EditText
    private lateinit var daysOfWeekTextViews: List<TextView>

    private val inflater by lazy { LayoutInflater.from(this) }
    private val medications = mutableListOf<Medication>()
    private val medicationTimes = mutableMapOf<String, MutableList<String>>()
    private val expandedItems = mutableMapOf<String, View>()
    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.medicine_add)

        initializeRetrofit()

        // Initialize UI elements from medicine_add.xml
        medicationEditText = findViewById(R.id.medicationEditText)
        addButton = findViewById(R.id.addButton)
        medicationList = findViewById(R.id.medicationList)
        saveButton = findViewById(R.id.save_button)

        // Add button click listener
        addButton.setOnClickListener {
            val medicationName = medicationEditText.text.toString().trim()
            if (medicationName.isNotEmpty()) {
                addMedicationItem(medicationName)
                medicationEditText.text.clear()
            } else {
                Toast.makeText(this, "약 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // Save button click listener
        saveButton.setOnClickListener {
            collectMedications()
            val token = getTokenFromSession()
            medications.forEach { medication ->
                val request = MedicationRequest(
                    medicationName = medication.name,
                    times = medication.times,
                    startDate = convertDateFormat(startDateText.text.toString()),
                    endDate = if (checkBoxNoEndDate.isChecked) null else convertDateFormat(endDateText.text.toString()),
                    endless = checkBoxNoEndDate.isChecked,
                    isAlarm = switchAlarm.isChecked,
                    weeks = getSelectedDaysOfWeek(),
                    effectiveness = effectivenessEditText.text.toString(),
                    precautions = precautionsEditText.text.toString(),
                    storageMethod = storageMethodEditText.text.toString()
                )
                Log.d("MedicineRequest", request.toString())
                sendMedicationToServer(token, request)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
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
        return sharedPreferences.getString("AuthToken", "") ?: ""
    }

    private fun sendMedicationToServer(token: String, request: MedicationRequest) {
        retrofitService.addMedication("Bearer $token", request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MedicineAddModifyActivity, "약이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    Log.d("MedicineAdd", "약이 성공적으로 추가되었습니다: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@MedicineAddModifyActivity, "약 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.d("MedicineAdd", "약 추가에 실패했습니다: $errorBody")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@MedicineAddModifyActivity, "약 추가 중 오류가 발생했습니다: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("MedicineAdd", "약 추가 중 오류가 발생했습니다: ${t.message}")
            }
        })
    }
    private fun convertDateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = inputFormat.parse(date)
        return outputFormat.format(parsedDate)
    }
    private fun setupDatePickers(medicineDetailView: View) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        startDateText = medicineDetailView.findViewById(R.id.startDateText)
        endDateText = medicineDetailView.findViewById(R.id.endDateText)
        checkBoxNoEndDate = medicineDetailView.findViewById(R.id.checkBox)

        startDateText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = String.format("%d.%02d.%02d", selectedYear, selectedMonth + 1, selectedDay)
                    startDateText.text = selectedDate
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        endDateText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = String.format("%d.%02d.%02d", selectedYear, selectedMonth + 1, selectedDay)
                    endDateText.text = selectedDate
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun getSelectedDaysOfWeek(): List<String> {
        return daysOfWeekTextViews.filter { it.background.constantState == ContextCompat.getDrawable(this,
            R.drawable.light_purple_shape_2
        )?.constantState }
            .map { it.text.toString() }
    }


    // 약 항목 추가
    private fun addMedicationItem(medicationName: String) {
        val medicationNameLayout = inflater.inflate(R.layout.medicine_list_item, medicationList, false) as ConstraintLayout
        val detailLayout = inflater.inflate(R.layout.medicine_list_item_detail, medicationList, false) as ScrollView

        val medicationNameTextView = medicationNameLayout.findViewById<TextView>(R.id.titleTextView)
        medicationNameTextView.text = medicationName

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(35, 15, 35, 15)
        medicationNameLayout.layoutParams = layoutParams

        medicationNameLayout.setOnClickListener {
            toggleDetailViews(medicationName, detailLayout)
        }

        medicationList.addView(medicationNameLayout)
        medicationList.addView(detailLayout)
    }

    // 상세 정보 토글
    private fun toggleDetailViews(medicationName: String, detailLayout: ScrollView) {
        if (detailLayout.visibility == View.VISIBLE) {
            detailLayout.visibility = View.GONE
            expandedItems.remove(medicationName)
        } else {
            expandedItems.values.forEach {
                it.visibility = View.GONE
            }
            expandedItems.clear()
            detailLayout.visibility = View.VISIBLE
            expandedItems[medicationName] = detailLayout
            populateDetailViews(medicationName, detailLayout)
        }
    }

    // 상세 정보 채우기
    private fun populateDetailViews(medicationName: String, detailLayout: ScrollView) {
        val signUpDetailView = detailLayout.findViewById<ConstraintLayout>(R.id.sign_up_4_detail)
        val medicineDetailView = detailLayout.findViewById<ConstraintLayout>(R.id.medicine_add_detail)

        val addTimeButton = signUpDetailView.findViewById<Button>(R.id.add_time_button)
        val timeListLayout = signUpDetailView.findViewById<LinearLayout>(R.id.time_list_layout)
        val timePicker = signUpDetailView.findViewById<TimePicker>(R.id.timePicker)

        timeListLayout.removeAllViews()

        medicationTimes[medicationName]?.forEach { time ->
            addTimeEntry(timeListLayout, time)
        }

        addTimeButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val time = String.format("%02d:%02d", hour, minute)
            addTimeEntry(timeListLayout, time)
            medicationTimes.getOrPut(medicationName) { mutableListOf() }.add(time)
        }

        startDateText = medicineDetailView.findViewById(R.id.startDateText)
        endDateText = medicineDetailView.findViewById(R.id.endDateText)
        checkBoxNoEndDate = medicineDetailView.findViewById(R.id.checkBox)

        val startDateIcon = medicineDetailView.findViewById<ImageView>(R.id.startDateIcon)
        val endDateIcon = medicineDetailView.findViewById<ImageView>(R.id.endDateIcon)

        startDateIcon.setOnClickListener {
            showDatePickerDialog(startDateText)
        }

        endDateIcon.setOnClickListener {
            showDatePickerDialog(endDateText)
        }

        checkBoxNoEndDate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                endDateText.isEnabled = false
                endDateIcon.isEnabled = false
                endDateText.setTextColor(Color.GRAY)
                endDateText.setBackgroundResource(R.drawable.grey_box)
            } else {
                endDateText.isEnabled = true
                endDateIcon.isEnabled = true
                endDateText.setTextColor(Color.BLACK)
                endDateText.setBackgroundResource(R.drawable.bottom_stroke_grey)
            }
        }

        daysOfWeekTextViews = listOf(
            medicineDetailView.findViewById(R.id.textViewMon),
            medicineDetailView.findViewById(R.id.textViewTue),
            medicineDetailView.findViewById(R.id.textViewWed),
            medicineDetailView.findViewById(R.id.textViewThu),
            medicineDetailView.findViewById(R.id.textViewFri),
            medicineDetailView.findViewById(R.id.textViewSat),
            medicineDetailView.findViewById(R.id.textViewSun)
        )

        daysOfWeekTextViews.forEach { dayTextView ->
            dayTextView.setOnClickListener {
                val selectedBackground: Drawable? = ContextCompat.getDrawable(this,
                    R.drawable.light_purple_shape_2
                )
                val defaultBackground: Drawable? = ContextCompat.getDrawable(this,
                    R.drawable.light_purple_shape
                )

                dayTextView.background = if (dayTextView.background.constantState == selectedBackground?.constantState) {
                    defaultBackground
                } else {
                    selectedBackground
                }
            }
        }

        setupDatePickers(medicineDetailView)

        switchAlarm = medicineDetailView.findViewById(R.id.switchAlarm)
        effectivenessEditText = medicineDetailView.findViewById(R.id.direct_input_text)
        precautionsEditText = medicineDetailView.findViewById(R.id.direct_input_text2)
        storageMethodEditText = medicineDetailView.findViewById(R.id.direct_input_text3)
    }

    // 시간 항목 추가
    private fun addTimeEntry(timeListLayout: LinearLayout, time: String) {
        val timeEntryView = inflater.inflate(R.layout.time_item, timeListLayout, false)
        val timeTextView = timeEntryView.findViewById<TextView>(R.id.timeTextView)
        val removeButton = timeEntryView.findViewById<ImageView>(R.id.removeButton)

        timeTextView.text = time

        removeButton.setOnClickListener {
            timeListLayout.removeView(timeEntryView)
            medicationTimes.values.forEach { times ->
                times.remove(time)
            }
        }

        timeListLayout.addView(timeEntryView)
    }

    // 날짜 선택 다이얼로그 표시
    private fun showDatePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%d.%02d.%02d", selectedYear, selectedMonth + 1, selectedDay)
                textView.text = selectedDate
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    // 약 정보 수집
    private fun collectMedications() {
        medications.clear()
        for (i in 0 until medicationList.childCount step 2) {
            val medicationView = medicationList.getChildAt(i) as ConstraintLayout
            val medicationName = medicationView.findViewById<TextView>(R.id.titleTextView).text.toString()
            val times = medicationTimes[medicationName] ?: mutableListOf()

            medications.add(Medication(medicationName, times))
        }
    }
}
