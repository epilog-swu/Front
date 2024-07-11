// ActivityShowFailDialog.kt
package com.epi.epilog

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityShowFailDialog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_fail)

        // Set the layout parameters to match parent for the root view
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val dialogYesBtn: Button = findViewById(R.id.dialog_yes_btn)

        // Confirm action when the "확인" button is clicked
        dialogYesBtn.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0) // No animation on exit
    }
}
