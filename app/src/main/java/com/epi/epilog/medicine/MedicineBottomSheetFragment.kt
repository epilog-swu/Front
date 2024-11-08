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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        if (parentFragment !is MedicineChecklistFragment) {
            Log.e("MedicineBottomSheetFragment", "Warning: This fragment was not attached to MedicineChecklistFragment")
        } else {
            Log.d("MedicineBottomSheetFragment", "Attached to MedicineChecklistFragment")
        }
    }

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
        val goalTimeFromArgs = arguments?.getString("goal_time") // 추가된 부분
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
            // 서버에서 받은 goalTime을 특정 포맷으로 변환하여 사용
            val goalTimeFromArgs = arguments?.getString("goal_time")
            val formattedGoalTime = goalTimeFromArgs?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }

            if (formattedGoalTime != null) {
                if (checklistItemId != null) {
                    //서버 업데이트
                    updateMedicineStatus(checklistItemId,State.복용,formattedGoalTime)
                }
                //UI 업데이트
                applyChanges(State.복용, formattedGoalTime, selectedDate)
            } else {
                Log.e("MedicineBottomSheetFragment", "Error: goalTime is not properly formatted or missing")
            }
        }

        //TODO: 보내는 시간 GOALTIME 인지 확인. 제대로 보내고 있는지 확인해야함
        binding.bottomButton2.setOnClickListener {

            // TimePicker를 통해 시간을 선택하도록 BottomSheet2 열기
            val timePickerFragment = MedicineBottomSheetFragment2()
            timePickerFragment.show(parentFragmentManager, "timePicker")

            // 서버에서 받은 goalTime을 특정 포맷으로 변환하여 사용
            val goalTimeFromArgs = arguments?.getString("goal_time")

            val formattedGoalDate = goalTimeFromArgs?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }

            if (checklistItemId != null) {
                // TimePicker에서 선택된 시간 수신 대기
                parentFragmentManager.setFragmentResultListener("timePickerRequestKey", this) { _, bundle ->
                    selectedTimeFromPicker = bundle.getString("selectedTime")
                    // 선택된 시간이 있을 경우 서버, UI 업데이트
                    selectedTimeFromPicker?.let { time ->
                        // formattedGoalDate와 selectedTime을 결합하여 새로운 문자열 생성
                        val combinedDateTime = "$formattedGoalDate $time"
                        updateMedicineStatus(checklistItemId, State.복용, combinedDateTime)
                        applyChanges(State.복용, combinedDateTime, selectedDate)
                    }
                }
            } else {
                Log.e("MedicineBottomSheetFragment", "ErrorCase2: checklistItemid is not properly formatted or missing")
            }
        }


        //TODO : 3번 해야함.
        binding.bottomButton3.setOnClickListener {
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            if (currentTime != null) {
                if (checklistItemId != null) {
                    //서버 업데이트
                    updateMedicineStatus(checklistItemId,State.미복용,currentTime)
                }
                //UI 업데이트
                applyChanges(State.미복용, currentTime, selectedDate)
            } else {
                Log.e("MedicineBottomSheetFragment", "Error: goalTime is not properly formatted or missing")
            }
        }
    }

    private fun getTokenFromSession(): String {
        val sharedPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("AuthToken", "") ?: ""
    }

    //서버 상태 업데이트
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

        // Request body 내용을 로그로 출력
        Log.d("updateMedicineStatus", "Request body: time = ${requestBody.time}, status = ${requestBody.status}")

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

    //UI 업데이트
    private fun applyChanges(newState: State, time: String, selectedDate: LocalDate?) {
        val checklistItemId = arguments?.getInt("checklist_item_id")
        Log.d("applyChanges", "Applying changes to item ID: $checklistItemId with state: $newState at $time")

        // 로그 추가 예시
        checklistItemId?.let { id ->
            val parentFragment = parentFragment as? MedicineChecklistFragment

            // parentFragment가 null인지 확인하는 로그 추가
            if (parentFragment == null) {
                Log.d("applyChanges", "parentFragment is null")
            } else {
                Log.d("applyChanges", "parentFragment is not null and is of type: ${parentFragment::class.java.simpleName}")
            }

            parentFragment?.applyStateChangeToMedicineItem(id, newState)

//            // goalTime 설정
//            val goalTimeFromArgs = arguments?.getString("goal_time")
//            val formattedGoalTime = goalTimeFromArgs?.let {
//                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//            }
//
//            if (formattedGoalTime != null) {
//                updateMedicineStatus(id, newState, formattedGoalTime)
//            }
//            Log.d("applyChanges", "Called updateMedicineStatus for ID: $id")

            // 상태 변경 후 UI 강제 새로고침 로그 추가
            Log.d("applyChanges", "Invalidating RecyclerView and requesting layout refresh")
            parentFragment?.recyclerView?.invalidate()
            parentFragment?.recyclerView?.requestLayout()

            selectedDate?.let {
                parentFragment?.selectDate(it)
                Log.d("applyChanges", "Refetched data for date: $it")
            }
        }

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}