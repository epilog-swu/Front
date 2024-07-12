package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins

class signUp4Activity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var addButton: Button
    private lateinit var nextButton: Button
    private lateinit var medicationList: LinearLayout
    private val inflater by lazy { LayoutInflater.from(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_4)

        editText = findViewById(R.id.editTextText)
        addButton = findViewById(R.id.male_button)
        nextButton = findViewById(R.id.next_button)
        medicationList = findViewById(R.id.medication_list)

        addButton.setOnClickListener {
            val medicationName = editText.text.toString().trim()
            if (medicationName.isNotEmpty()) {
                addMedicationButton(medicationName)
                editText.text.clear()
            } else {
                Toast.makeText(this, "약 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        nextButton.setOnClickListener {
            val intent = Intent(this, signUp5Activity::class.java)
            startActivity(intent)
        }
    }

    private fun addMedicationButton(medicationName: String) {
        val button = Button(this).apply {
            text = medicationName

            setBackgroundResource(R.drawable.signup_button_shape)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.button_height)
            ).apply {
                setMargins(30, 0, 25, 16)
                setPadding(15, 0, 0, 0)

            }

            setGravity( Gravity.CENTER_VERTICAL)
            setOnClickListener {
                toggleMedicationDetailVisibility(this)
            }
        }
        medicationList.addView(button)
    }

    private fun toggleMedicationDetailVisibility(button: Button) {
        val parent = button.parent as LinearLayout
        var detailView = parent.findViewWithTag<View>("detail_${button.text}")

        if (detailView == null) {
            detailView = inflater.inflate(R.layout.sign_up_4_detail, parent, false).apply {
                tag = "detail_${button.text}"
                findViewById<Button>(R.id.add_time_button).setOnClickListener {
                    addTimeItem(this)
                }
            }
            parent.addView(detailView, parent.indexOfChild(button) + 1)
        } else {
            detailView.visibility = if (detailView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    private fun addTimeItem(detailView: View) {
        val timePicker = detailView.findViewById<TimePicker>(R.id.timePicker)
        val hour = timePicker.hour
        val minute = timePicker.minute
        val time = String.format("%02d:%02d", hour, minute)

        val timeItem = inflater.inflate(R.layout.time_item, null).apply {
            findViewById<TextView>(R.id.timeTextView).text = time
            findViewById<ImageView>(R.id.removeButton).setOnClickListener {
                (this.parent as ViewGroup).removeView(this)
            }
        }

        val timeListLayout = detailView.findViewById<LinearLayout>(R.id.time_list_layout)
        timeListLayout.addView(timeItem)
    }
}
