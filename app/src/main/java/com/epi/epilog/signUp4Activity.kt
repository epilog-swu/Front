package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.epi.epilog.presentation.theme.api.Medication

class signUp4Activity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var addButton: Button
    private lateinit var nextButton: Button
    private lateinit var medicationList: LinearLayout
    private lateinit var detailLayout: LinearLayout
    private var currentlyExpandedItem: View? = null
    private val inflater by lazy { LayoutInflater.from(this) }

    private val medications = mutableListOf<Medication>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_4)

        // Toolbar 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = getString(R.string.signup)

        editText = findViewById(R.id.editTextText)
        addButton = findViewById(R.id.male_button)
        nextButton = findViewById(R.id.button5)
        medicationList = findViewById(R.id.medication_list)
        detailLayout = findViewById(R.id.detailLayout)

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
            val intent = Intent(this, signUp5Activity::class.java).apply {
                putExtra("loginId", this@signUp4Activity.intent.getStringExtra("loginId"))
                putExtra("password", this@signUp4Activity.intent.getStringExtra("password"))
                putExtra("name", this@signUp4Activity.intent.getStringExtra("name"))
                putExtra("stature", this@signUp4Activity.intent.getFloatExtra("stature", 0f))
                putExtra("weight", this@signUp4Activity.intent.getFloatExtra("weight", 0f))
                putExtra("gender", this@signUp4Activity.intent.getStringExtra("gender"))
                putExtra("protectorName", this@signUp4Activity.intent.getStringExtra("protectorName"))
                putExtra("protectorPhone", this@signUp4Activity.intent.getStringExtra("protectorPhone"))
                putParcelableArrayListExtra("medications", ArrayList(medications))
            }

            startActivity(intent)
        }
    }

    private fun Intent.putParcelableArrayListExtra(key: String, value: ArrayList<Medication>): Intent {
        return this.putParcelableArrayListExtra(key, value)
    }

    private fun addMedicationItem(medicationName: String) {
        val medicationItem = inflater.inflate(R.layout.medicine_list_item, medicationList, false) as ConstraintLayout
        medicationItem.setBackgroundResource(R.drawable.meal_time_box)

        val medicationNameTextView = medicationItem.findViewById<TextView>(R.id.titleTextView)
        medicationNameTextView.text = medicationName

        medicationItem.setOnClickListener {
            toggleDetailViews(medicationItem, medicationName)
        }

        medicationList.addView(medicationItem)
    }

    private fun toggleDetailViews(medicationItem: View, medicationName: String) {
        if (currentlyExpandedItem == medicationItem) {
            detailLayout.removeAllViews()
            currentlyExpandedItem = null
        } else {
            detailLayout.removeAllViews()
            showDetailViews(medicationName)
            currentlyExpandedItem = medicationItem
        }
    }

    private fun showDetailViews(medicationName: String) {
        val signUpDetailView = inflater.inflate(R.layout.sign_up_4_detail, detailLayout, false) as ConstraintLayout

        val addTimeButton = signUpDetailView.findViewById<Button>(R.id.add_time_button)
        val timePicker = signUpDetailView.findViewById<TimePicker>(R.id.timePicker)
        val timeListLayout = signUpDetailView.findViewById<LinearLayout>(R.id.time_list_layout)

        addTimeButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            addTimeEntry(timeListLayout, hour, minute)
        }

        detailLayout.addView(signUpDetailView)
    }

    private fun addTimeEntry(timeListLayout: LinearLayout, hour: Int, minute: Int) {
        val timeEntryView = inflater.inflate(R.layout.time_item, timeListLayout, false)
        val timeTextView = timeEntryView.findViewById<TextView>(R.id.timeTextView)
        val removeButton = timeEntryView.findViewById<ImageView>(R.id.removeButton)

        val selectedTime = String.format("%02d:%02d", hour, minute)
        timeTextView.text = selectedTime

        removeButton.setOnClickListener {
            timeListLayout.removeView(timeEntryView)
        }

        timeListLayout.addView(timeEntryView)
    }

    private fun collectMedications() {
        medications.clear()
        for (i in 0 until medicationList.childCount) {
            val medicationView = medicationList.getChildAt(i) as ConstraintLayout
            val medicationName = medicationView.findViewById<TextView>(R.id.titleTextView).text.toString()
            val times = mutableListOf<String>()

            val timeListLayout = detailLayout.findViewById<LinearLayout>(R.id.time_list_layout)
            for (j in 0 until timeListLayout.childCount) {
                val timeItem = timeListLayout.getChildAt(j)
                val time = timeItem.findViewById<TextView>(R.id.timeTextView).text.toString()
                times.add(time)
            }

            medications.add(Medication(medicationName, times))
        }
    }
}
