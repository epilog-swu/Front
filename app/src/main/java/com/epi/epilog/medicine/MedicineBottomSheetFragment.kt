package com.epi.epilog.medicine

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epi.epilog.ApiResponse
import com.epi.epilog.R
import com.epi.epilog.api.MedicationStatusUpdateRequest
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.State
import com.epi.epilog.databinding.FragmentMedicineSelectBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MedicineBottomSheetFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.RoundedBottomSheetDialog
    }

    private var _binding: FragmentMedicineSelectBottomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineSelectBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val checklistItemId = arguments?.getInt("checklist_item_id")
        val parentFragment = parentFragment as? MedicineChecklistFragment

        // FragmentResultListener 등록
        parentFragmentManager.setFragmentResultListener("timePickerRequestKey", this) { _, bundle ->
            val selectedTime = bundle.getString("selectedTime")
            if (selectedTime != null && checklistItemId != null) {
                applyChanges(State.복용, selectedTime)
            }
        }

        val applyChanges = { newState: State, time: String ->
            checklistItemId?.let { id ->
                // UI 업데이트
                parentFragment?.applyStateChangeToMedicineItem(id, newState)

                // 서버에 상태 변경 요청 전송
                updateMedicineStatus(id, newState, time)
                Log.d("MedicineBottomSheetFragment", "ID: $id changed to State: $newState at $time")
            }
            dismiss()
        }

        binding.bottomButton1.setOnClickListener {
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            applyChanges(State.복용, currentTime)
        }

        binding.bottomButton2.setOnClickListener {
            val timePickerFragment = MedicineBottomSheetFragment2()
            timePickerFragment.show(parentFragmentManager, "timePicker")
        }

        binding.bottomButton3.setOnClickListener {
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            applyChanges(State.미복용, currentTime)
        }
    }
    fun getTokenFromSession(): String {
        val sharedPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("AuthToken", "") ?: ""
    }
    private fun updateMedicineStatus(checklistItemId: Int, newState: State, time: String) {
        val token = getTokenFromSession()

        if (token.isNullOrBlank()) {
            Log.e("MedicineChecklistFragment", "Token is missing or invalid")
            return
        }

        val requestBody = MedicationStatusUpdateRequest(
            time = time,
            status = newState
        )

        RetrofitClient.retrofitService.updateMedicineStatus(checklistItemId, "Bearer $token", requestBody).enqueue(object :
            Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Log.d("MedicineChecklistFragment", "Status updated successfully for ID: $checklistItemId")
                } else {
                    Log.e("MedicineChecklistFragment", "Failed to update status for ID: $checklistItemId. Response code: ${response.code()}, message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("MedicineChecklistFragment", "Error updating status for ID: $checklistItemId", t)
            }
        })
    }
    private fun applyChanges(newState: State, time: String) {
        val checklistItemId = arguments?.getInt("checklist_item_id")

        checklistItemId?.let { id ->
            // UI 업데이트
            val parentFragment = parentFragment as? MedicineChecklistFragment
            parentFragment?.applyStateChangeToMedicineItem(id, newState)

            // 서버에 상태 변경 요청 전송
            updateMedicineStatus(id, newState, time)
            Log.d("MedicineBottomSheetFragment", "ID: $id changed to State: $newState at $time")
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
