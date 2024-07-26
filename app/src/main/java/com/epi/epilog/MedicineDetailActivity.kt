package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MedicineDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.medicine_detail)


        findViewById<Button>(R.id.adjust_button).setOnClickListener {
            showDeleteDialog()
        }

        findViewById<Button>(R.id.delete_button).setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        val dialog = DeleteDialogFragment()
        dialog.show(supportFragmentManager, "DeleteDialogFragment")
    }
}
