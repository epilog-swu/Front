// ActivityShowSuccessDialog.kt
package com.epi.epilog

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityShowSuccessDialog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Override the transition animations for entering the activity
        overridePendingTransition(0, 0)

        setContentView(R.layout.dialog_success) // Ensure you have this layout file

        // Set the layout parameters to match parent for the root view
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val dialogNoBtn: Button = findViewById(R.id.dialog_no_btn)
        val dialogYesBtn: Button = findViewById(R.id.dialog_yes_btn)

        // Close the dialog when the "취소" button is clicked
        dialogNoBtn.setOnClickListener {
            finish()
        }

        // Close the dialog when the "확인" button is clicked
        dialogYesBtn.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        // Override the transition animations for exiting the activity
        overridePendingTransition(0, 0)
    }
}
