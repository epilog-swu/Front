package com.epi.epilog

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DeleteDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.medicine_dialog, null)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundCornerDialogStyle)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.dialogCancleBtn).setOnClickListener {
            Toast.makeText(context, "수정되었습니다", Toast.LENGTH_SHORT).show()
            navigateToChecklistFragment()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.dialogOkBtn).setOnClickListener {
            Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
            navigateToChecklistFragment()
            dialog.dismiss()
        }

        return dialog
    }

    private fun navigateToChecklistFragment() {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("navigateToFragment", "MedicineChecklistFragment")
        startActivity(intent)
    }
}
