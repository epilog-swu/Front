import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.epi.epilog.main.MainActivity
import com.epi.epilog.R

class DeleteDialogFragment : DialogFragment() {

    private var medicationId: Int? = null

    companion object {
        private const val ARG_MEDICATION_ID = "medication_id"
        private const val ARG_NEXT_ID = "nextId"
        private const val ARG_PREV_ID = "prevId"

        fun newInstance(medicationId: Int?, nextId: Int?, prevId: Int?): DeleteDialogFragment {
            val fragment = DeleteDialogFragment()
            val args = Bundle()
            args.putInt(ARG_MEDICATION_ID, medicationId ?: -1)
            args.putInt(ARG_NEXT_ID, nextId ?: -1)
            args.putInt(ARG_PREV_ID, prevId ?: -1)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        medicationId = arguments?.getInt(ARG_MEDICATION_ID)

        val dialogView = layoutInflater.inflate(R.layout.medicine_dialog, null)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundCornerDialogStyle)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.dialogCancleBtn).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.dialogOkBtn).setOnClickListener {
            Toast.makeText(context, "약이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            navigateToMainActivity() // MainActivity로 이동
        }

        return dialog
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        activity?.finish()
    }
}
