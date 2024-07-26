package com.epi.epilog

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import java.util.*

class MedicineAddModifyActivity : AppCompatActivity() {

    private lateinit var medicationEditText: EditText
    private lateinit var addButton: Button
    private lateinit var medicationList: LinearLayout
    private lateinit var detailLayout: LinearLayout
    private var currentlyExpandedItem: View? = null
    private lateinit var endDateText: TextView
    private lateinit var endDateIcon: ImageView
    private lateinit var checkBoxNoEndDate: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.medicine_add)

        medicationEditText = findViewById(R.id.medicationEditText)
        addButton = findViewById(R.id.addButton)
        medicationList = findViewById(R.id.medicationList)
        detailLayout = findViewById(R.id.detailLayout)

        addButton.setOnClickListener {
            addMedicationItem(medicationEditText.text.toString())
        }

        findViewById<Button>(R.id.save_button).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("navigateToFragment", "MedicineChecklistFragment")
            }
            Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

    }

    private fun addMedicationItem(medicationName: String) {
        if (medicationName.isNotEmpty()) {
            val medicationItem = LayoutInflater.from(this).inflate(R.layout.medicine_list_item, medicationList, false) as ConstraintLayout
            medicationItem.setBackgroundResource(R.drawable.meal_time_box)

            val medicationNameTextView = medicationItem.findViewById<TextView>(R.id.titleTextView)
            medicationNameTextView.text = medicationName

            medicationItem.setOnClickListener {
                toggleDetailViews(medicationItem, medicationName)
            }

            medicationList.addView(medicationItem)
            medicationEditText.text.clear()
        } else {
            Toast.makeText(this, "약 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
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
        val signUpDetailView = LayoutInflater.from(this).inflate(R.layout.sign_up_4_detail, detailLayout, false) as ConstraintLayout
        val medicineDetailView = LayoutInflater.from(this).inflate(R.layout.medicine_add_detail, detailLayout, false) as ConstraintLayout

        val addTimeButton = signUpDetailView.findViewById<Button>(R.id.add_time_button)
        val timeListLayout = signUpDetailView.findViewById<LinearLayout>(R.id.time_list_layout)

        addTimeButton.setOnClickListener {
            addTimeEntry(timeListLayout)
        }

        detailLayout.addView(signUpDetailView)
        detailLayout.addView(medicineDetailView)

        val startDateText = medicineDetailView.findViewById<TextView>(R.id.startDateText)
        endDateText = medicineDetailView.findViewById(R.id.endDateText)
        val startDateIcon = medicineDetailView.findViewById<ImageView>(R.id.startDateIcon)
        endDateIcon = medicineDetailView.findViewById(R.id.endDateIcon)
        checkBoxNoEndDate = medicineDetailView.findViewById(R.id.checkBox)

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
                endDateText.setBackgroundColor(Color.LTGRAY)
            } else {
                endDateText.isEnabled = true
                endDateIcon.isEnabled = true
                endDateText.setTextColor(Color.BLACK)
                endDateText.setBackgroundResource(R.drawable.bottom_stroke_grey)
            }
        }

        val daysOfWeek = listOf(
            medicineDetailView.findViewById<TextView>(R.id.textViewMon),
            medicineDetailView.findViewById<TextView>(R.id.textViewTue),
            medicineDetailView.findViewById<TextView>(R.id.textViewWed),
            medicineDetailView.findViewById<TextView>(R.id.textViewThu),
            medicineDetailView.findViewById<TextView>(R.id.textViewFri),
            medicineDetailView.findViewById<TextView>(R.id.textViewSat),
            medicineDetailView.findViewById<TextView>(R.id.textViewSun)
        )

        daysOfWeek.forEach { dayTextView ->
            dayTextView.setOnClickListener {
                val selectedBackground: Drawable? = ContextCompat.getDrawable(this, R.drawable.light_purple_shape_2)
                val defaultBackground: Drawable? = ContextCompat.getDrawable(this, R.drawable.light_purple_shape)

                dayTextView.background = if (dayTextView.background.constantState == selectedBackground?.constantState) {
                    defaultBackground
                } else {
                    selectedBackground
                }
            }
        }
    }

    private fun addTimeEntry(timeListLayout: LinearLayout) {
        val timeEntryView = LayoutInflater.from(this).inflate(R.layout.time_item, timeListLayout, false)

        val removeButton = timeEntryView.findViewById<ImageView>(R.id.removeButton)
        removeButton.setOnClickListener {
            timeListLayout.removeView(timeEntryView)
        }

        timeListLayout.addView(timeEntryView)
    }

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
}
