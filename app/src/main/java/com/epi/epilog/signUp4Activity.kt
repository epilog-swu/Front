package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class signUp4Activity : AppCompatActivity() {

    private lateinit var editTextMedicine: EditText
    private lateinit var buttonAdd: Button
    private lateinit var layoutMedicines: LinearLayout
    private lateinit var buttonNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_4)

        editTextMedicine = findViewById(R.id.editTextText)
        buttonAdd = findViewById(R.id.male_button)
        layoutMedicines = findViewById(R.id.layoutMedicines)
        buttonNext = findViewById(R.id.next_button)

        buttonAdd.setOnClickListener {
            addMedicineBadge()
        }

        buttonNext.setOnClickListener {
            val intent = Intent(this, signUp5Activity::class.java)
            startActivity(intent)
        }
    }

    private fun addMedicineBadge() {
        val medicineName = editTextMedicine.text.toString().trim()
        if (medicineName.isNotEmpty()) {
            val badge = TextView(this).apply {
                text = medicineName
                setBackgroundResource(R.drawable.badge)
                setPadding(16, 8, 16, 8)
                setTextColor(resources.getColor(R.color.black))
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 0, 0, 0)
            }

            badge.layoutParams = params
            layoutMedicines.addView(badge)
            editTextMedicine.text.clear()

            Toast.makeText(this, "약이 추가되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "약 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
