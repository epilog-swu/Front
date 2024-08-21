package com.epi.epilog.medicine

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.epi.epilog.ApiResponse
import com.epi.epilog.LoginActivity
import com.epi.epilog.R
import com.epi.epilog.api.ChecklistItem
import com.epi.epilog.api.MedicationChecklistResponse
import com.epi.epilog.api.MedicationStatusUpdateRequest
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.State
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MedicineChecklistFragment : Fragment() {

    private var selectedDate: LocalDate? = LocalDate.now()
    private lateinit var weekCalendarView: WeekCalendarView
    private var medicationId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine_checklist, container, false)

        view.findViewById<Button>(R.id.add_existing_medicine_button).setOnClickListener {
            val intent = Intent(context, MedicineDetailActivity::class.java)
            if (medicationId != null) {
                intent.putExtra("medicationId", medicationId)
                startActivity(intent)
            } else {
                Toast.makeText(context, "No medication ID found.", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.add_medicine_button).setOnClickListener {
            startActivity(Intent(context, MedicineAddModifyActivity::class.java))
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        validateToken()

        weekCalendarView = view.findViewById(R.id.meal_calendarView)
        initWeekCalendarView()
    }

    private fun validateToken() {
        val token = getTokenFromSession()
        if (token.isEmpty()) {
            redirectToLogin()
            return
        }
        RetrofitClient.retrofitService.testApi("Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    selectDate(LocalDate.now()) // 초기화 시 오늘 날짜 데이터 로드
                } else {
                    redirectToLogin()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                redirectToLogin()
            }
        })
    }

    private fun redirectToLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun getTokenFromSession(): String {
        val sharedPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("AuthToken", "") ?: ""
    }

    private fun selectDate(date: LocalDate) {
        selectedDate = date
        fetchMedicationChecklist(date)
    }

    private fun fetchMedicationChecklist(date: LocalDate) {
        val dateString = date.toString()
        val token = "Bearer " + getTokenFromSession()

        RetrofitClient.retrofitService.getMedicationChecklist(dateString, token).enqueue(object :
            Callback<MedicationChecklistResponse> {
            override fun onResponse(call: Call<MedicationChecklistResponse>, response: Response<MedicationChecklistResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { fetchedResponse ->
                        medicationId = fetchedResponse.medicationId // medicationId 설정

                        val checklistIds = fetchedResponse.checklist.map { it.id }
                        Log.d("MedicineChecklistFragment", "Selected Date: $date, Checklist IDs: $checklistIds, Medication ID: $medicationId")

                        updateChecklistUI(fetchedResponse.checklist.filter { it.goalTime.startsWith(dateString) })
                    }
                } else {
                    Toast.makeText(context, "Failed to load medication checklist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MedicationChecklistResponse>, t: Throwable) {
                Toast.makeText(context, "Error fetching checklist: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateChecklistUI(checklist: List<ChecklistItem>) {
        val medicineContentLayout = view?.findViewById<LinearLayout>(R.id.medicine_content_layout)
        medicineContentLayout?.removeAllViews()

        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (item in checklist) {
            val itemView = layoutInflater.inflate(R.layout.fragment_medicine_checklist_item, medicineContentLayout, false)
            val medicineTime = itemView.findViewById<TextView>(R.id.medicine_time)
            val medicineName = itemView.findViewById<TextView>(R.id.medicine_name)
            val medicineCheckbox = itemView.findViewById<CheckBox>(R.id.medicine_checkbox)

            val goalTime = LocalDateTime.parse(item.goalTime, formatter)

            medicineTime.text = item.time
            medicineName.text = item.medicationName
            medicineCheckbox.isChecked = item.isComplete

            Log.d("MedicineChecklistFragment", "ID: ${item.id}, State: ${item.state}, Time: ${item.time}, Medication: ${item.medicationName}")

            updateItemViewBasedOnState(item.state, medicineName, medicineTime, itemView, medicineCheckbox)

            val showBottomSheet: (ChecklistItem) -> Unit = { selectedItem ->
                val bottomSheetFragment = MedicineBottomSheetFragment().apply {
                    arguments = Bundle().apply {
                        putInt("checklist_item_id", selectedItem.id)  // 아이템 ID 전달
                    }
                }
                bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
            }

            // Container 클릭 시
            itemView.setOnClickListener {
                showBottomSheet(item)
            }

            // 체크박스 클릭 시
            medicineCheckbox.setOnClickListener {

                showBottomSheet(item)

                val newState = if (medicineCheckbox.isChecked) State.복용 else State.미복용
                val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                // UI를 먼저 업데이트
                applyStateChangeToMedicineItem(item.id, newState)

                // 서버에 상태 변경 요청 전송
                updateMedicineStatus(item.id, newState, currentTime, onFailure = {
                    // 요청이 실패한 경우 UI 롤백
                    applyStateChangeToMedicineItem(item.id, if (newState == State.복용) State.미복용 else State.복용)
                })
            }

            medicineContentLayout?.addView(itemView)
        }
    }

    private fun updateMedicineStatus(checklistItemId: Int, newState: State, time: String, onFailure: (() -> Unit)? = null) {
        val token = getTokenFromSession()

        if (token.isNullOrBlank()) {
            Log.e("MedicineChecklistFragment", "Token is missing or invalid")
            onFailure?.invoke()
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
                    onFailure?.invoke()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("MedicineChecklistFragment", "Error updating status for ID: $checklistItemId", t)
                onFailure?.invoke()
            }
        })
    }


    private fun updateItemViewBasedOnState(
        state: State,
        medicineName: TextView,
        medicineTime: TextView,
        itemView: View,
        medicineCheckbox: CheckBox
    ) {
        when (state) {
            State.복용 -> {
                applyStrikeThrough(medicineName, medicineTime, true)
                itemView.setBackgroundResource(R.drawable.medicine_background)
                medicineCheckbox.isChecked = true
            }
            State.미복용 -> {
                applyStrikeThrough(medicineName, medicineTime, false)
                itemView.setBackgroundResource(R.drawable.background_red)
                medicineCheckbox.isChecked = false
            }
            State.상태없음 -> {
                applyStrikeThrough(medicineName, medicineTime, false)
                itemView.setBackgroundResource(R.drawable.background_yellow)
                medicineCheckbox.isChecked = false
            }
        }
    }



    private fun applyStrikeThrough(medicineName: TextView, medicineTime: TextView, isComplete: Boolean) {
        if (isComplete) {
            medicineName.paintFlags = medicineName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            medicineTime.paintFlags = medicineTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            medicineName.paintFlags = medicineName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            medicineTime.paintFlags = medicineTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    private fun initWeekCalendarView() {
        weekCalendarView.dayBinder = object :
            WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.app_week_cal_selected_date)
                } else {
                    container.textView.setBackgroundResource(0) // Default background
                }

                container.textView.setOnClickListener {
                    onDateSelected(data.date)
                }
            }
        }

        val currentDate = LocalDate.now()
        weekCalendarView.setup(
            currentDate.minusMonths(100).withDayOfMonth(1),
            currentDate.plusMonths(100).withDayOfMonth(currentDate.lengthOfMonth()),
            DayOfWeek.SUNDAY
        )
        weekCalendarView.scrollToWeek(currentDate)
    }

    private fun onDateSelected(date: LocalDate) {
        val currentSelection = selectedDate
        selectedDate = date

        weekCalendarView.notifyDateChanged(date)
        if (currentSelection != null && currentSelection != date) {
            weekCalendarView.notifyDateChanged(currentSelection)
        }

        Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()

        selectDate(date)
    }

    fun applyStateChangeToMedicineItem(medicineItemId: Int, newState: State) {
        view?.findViewWithTag<View>("item-$medicineItemId")?.let { itemView ->
            val medicineNameTextView = itemView.findViewById<TextView>(R.id.medicine_name)
            val medicineTimeTextView = itemView.findViewById<TextView>(R.id.medicine_time)
            val medicineCheckbox = itemView.findViewById<CheckBox>(R.id.medicine_checkbox)

            // UI 업데이트
            updateItemViewBasedOnState(newState, medicineNameTextView, medicineTimeTextView, itemView, medicineCheckbox)
        }
    }




    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.weekcalendarDayText)
    }
}
