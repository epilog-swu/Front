package com.epi.epilog.medicine

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epi.epilog.R
import com.epi.epilog.api.ApiResponse
import com.epi.epilog.api.MedicationStatusUpdateRequest
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.State
import com.epi.epilog.databinding.FragmentMedicineSelectBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MedicineBottomSheetFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.RoundedBottomSheetDialog
    }

    private var _binding: FragmentMedicineSelectBottomBinding? = null
    private val binding get() = _binding!!

    private var selectedTimeFromPicker: String? = null

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
        val selectedDate = arguments?.getString("selected_date")?.let { LocalDate.parse(it) }
        val parentFragment = parentFragment as? MedicineChecklistFragment

        // TimePicker에서 선택된 시간 수신 대기
        parentFragmentManager.setFragmentResultListener("timePickerRequestKey", this) { _, bundle ->
            selectedTimeFromPicker = bundle.getString("selectedTime")
            // 선택된 시간이 있을 경우 서버에 적용
            selectedTimeFromPicker?.let {
                applyChanges(State.복용, it, selectedDate)
            }
        }

        binding.bottomButton1.setOnClickListener {
            val goalTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            applyChanges(State.복용, goalTime, selectedDate)
        }

        binding.bottomButton2.setOnClickListener {
            // TimePicker를 통해 시간을 선택하도록 BottomSheet2 열기
            val timePickerFragment = MedicineBottomSheetFragment2()
            timePickerFragment.show(parentFragmentManager, "timePicker")
        }

        binding.bottomButton3.setOnClickListener {
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            applyChanges(State.미복용, currentTime, selectedDate)
        }
    }

    private fun getTokenFromSession(): String {
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

    private fun applyChanges(newState: State, time: String, selectedDate: LocalDate?) {
        val checklistItemId = arguments?.getInt("checklist_item_id")

        checklistItemId?.let { id ->
            val parentFragment = parentFragment as? MedicineChecklistFragment
            parentFragment?.applyStateChangeToMedicineItem(id, newState)

            updateMedicineStatus(id, newState, time)
            Log.d("MedicineBottomSheetFragment", "ID: $id changed to State: $newState at $time")

            // 상태가 변경되면 선택된 날짜의 데이터를 자동으로 새로고침
            selectedDate?.let {
                parentFragment?.selectDate(it)
                Log.d("MedicineBottomSheetFragment", "날짜: $it")
            }
        }

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}