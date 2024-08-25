package com.epi.epilog.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.epi.epilog.R
import com.epi.epilog.api.Medication
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.SignUpRequest
import com.epi.epilog.api.SignUpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class signUp4Activity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var addButton: Button
    private lateinit var nextButton: Button
    private lateinit var medicationList: LinearLayout
    private var currentlyExpandedItem: View? = null
    private lateinit var inflater: LayoutInflater
    private val medications = mutableListOf<Medication>()
    private val medicationTimes = mutableMapOf<String, MutableList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_4)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = getString(R.string.signup)

        editText = findViewById(R.id.editTextText)
        addButton = findViewById(R.id.male_button)
        nextButton = findViewById(R.id.button5)
        medicationList = findViewById(R.id.medication_list)
        inflater = LayoutInflater.from(this)

        addButton.setOnClickListener {
            val medicationName = editText.text.toString().trim()
            if (medicationName.isNotEmpty()) {
                addMedicationItem(medicationName)
                editText.text.clear()
            } else {
                Toast.makeText(this, "약 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        nextButton.setOnClickListener {
            collectMedications()
            logMedications() // 로그 출력
            signUp()
        }
    }

    private fun signUp() {
        val loginId = intent.getStringExtra("loginId") ?: return
        val password = intent.getStringExtra("password") ?: return
        val name = intent.getStringExtra("name") ?: return
        val stature = intent.getFloatExtra("stature", 0f)
        val weight = intent.getFloatExtra("weight", 0f)
        val gender = intent.getStringExtra("gender") ?: return
        val protectorName = intent.getStringExtra("protectorName") ?: return
        val protectorPhone = intent.getStringExtra("protectorPhone") ?: return

        val signUpRequest = SignUpRequest(
            loginId = loginId,
            password = password,
            name = name,
            stature = stature,
            weight = weight,
            gender = gender,
            protectorName = protectorName,
            protectorPhone = protectorPhone,
            medication = medications
        )

        RetrofitClient.retrofitService.signUp(signUpRequest).enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        val intent = Intent(this@signUp4Activity, signUp5Activity::class.java).apply {
                            putExtra("code", responseBody.code)
                            putExtra("token", responseBody.token)  // 토큰을 다음 Activity에 전달
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@signUp4Activity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@signUp4Activity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Toast.makeText(this@signUp4Activity, "회원가입 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMedicationItem(medicationName: String) {
        val medicationItem = inflater.inflate(R.layout.medicine_list_item, medicationList, false) as ConstraintLayout
        medicationItem.setBackgroundResource(R.drawable.meal_time_box)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(35, 15, 35, 15)
        medicationItem.layoutParams = layoutParams
        val medicationNameTextView = medicationItem.findViewById<TextView>(R.id.titleTextView)
        medicationNameTextView.text = medicationName

        val detailLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        medicationItem.setOnClickListener {
            toggleDetailViews(detailLayout, medicationName)
        }

        medicationList.addView(medicationItem)
        medicationList.addView(detailLayout)
    }

    private fun toggleDetailViews(detailLayout: LinearLayout, medicationName: String) {
        if (currentlyExpandedItem == detailLayout) {
            detailLayout.removeAllViews()
            currentlyExpandedItem = null
        } else {
            currentlyExpandedItem?.let {
                (it as LinearLayout).removeAllViews()
            }
            showDetailViews(detailLayout, medicationName)
            currentlyExpandedItem = detailLayout
        }
    }

    private fun showDetailViews(detailLayout: LinearLayout, medicationName: String) {
        val signUpDetailView = inflater.inflate(R.layout.sign_up_4_detail, detailLayout, false) as ConstraintLayout

        val addTimeButton = signUpDetailView.findViewById<Button>(R.id.add_time_button)
        val timePicker = signUpDetailView.findViewById<TimePicker>(R.id.timePicker)
        val timeListLayout = signUpDetailView.findViewById<LinearLayout>(R.id.time_list_layout)

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

        detailLayout.addView(signUpDetailView)
    }

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

    private fun collectMedications() {
        medications.clear()
        for (i in 0 until medicationList.childCount step 2) {
            val medicationView = medicationList.getChildAt(i) as ConstraintLayout
            val medicationName = medicationView.findViewById<TextView>(R.id.titleTextView).text.toString()
            val times = medicationTimes[medicationName] ?: mutableListOf()

            medications.add(Medication(medicationName, times))
        }
    }

    private fun logMedications() {
        for (medication in medications) {
            Log.d("Medication", "Name: ${medication.name}, Times: ${medication.times.joinToString()}")
        }
    }
}
