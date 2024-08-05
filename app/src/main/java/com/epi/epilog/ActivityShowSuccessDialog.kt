package com.epi.epilog

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityShowSuccessDialog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(0, 0)

        setContentView(R.layout.dialog_success)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val dialogNoBtn: Button = findViewById(R.id.dialogCancleBtn)
        val dialogYesBtn: Button = findViewById(R.id.dialogOkBtn)

        dialogNoBtn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        dialogYesBtn.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
