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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.epi.epilog.signup.LoginActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R
import com.epi.epilog.api.ApiResponse
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

class MedicineChecklistFragment : Fragment() {

    private var selectedDate: LocalDate? = LocalDate.now()
    private lateinit var weekCalendarView: WeekCalendarView
    private var medicationId: Int? = null
    private var checklistItems: MutableList<ChecklistItem> = mutableListOf()
    private lateinit var instructionTextView: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var medicineAdapter: MedicineAdapter // Define adapter here for easy access

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine_checklist, container, false)

        view.findViewById<Button>(R.id.add_existing_medicine_button).setOnClickListener {
            medicationId?.let {
                val intent = Intent(context, MedicineDetailActivity::class.java)
                intent.putExtra("medicationId", it)
                startActivity(intent)
            } ?: run {
                Toast.makeText(context, "No medication ID found.", Toast.LENGTH_SHORT).show()
                Log.e("MedicineChecklistFragment", "medicationId is null when trying to open MedicineDetailActivity")
            }
        }
        view.findViewById<Button>(R.id.add_medicine_button).setOnClickListener {
            startActivity(Intent(context, MedicineAddModifyActivity::class.java))
        }

        instructionTextView = view.findViewById(R.id.instruction_text_view)

        recyclerView = view.findViewById(R.id.medicine_content_layout)
        recyclerView.layoutManager = LinearLayoutManager(context)
        medicineAdapter = MedicineAdapter(checklistItems) { item ->
            val bottomSheetFragment = MedicineBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt("checklist_item_id", item.id)
                    putString("goal_time", item.goalTime)
                    putString("selected_date", selectedDate.toString()) // 선택된 날짜 전달
                }
            }
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
        recyclerView.adapter = medicineAdapter

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
                    selectDate(LocalDate.now())
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

    fun selectDate(date: LocalDate) {
        Log.d("selectDate", "Selecting date: $date")
        selectedDate = date
        fetchMedicationChecklist(date)
    }

    fun onDateSelected(date: LocalDate) {
        val currentSelection = selectedDate
        Log.d("onDateSelected", "Current selection: $currentSelection, New selection: $date")
        selectedDate = date

        weekCalendarView.notifyDateChanged(date)
        currentSelection?.let {
            if (it != date) {
                weekCalendarView.notifyDateChanged(it)
                Log.d("onDateSelected", "Notified calendar view to change date: $it")
            }
        }

        Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()
        selectDate(date)
    }

    private fun fetchMedicationChecklist(date: LocalDate) {
        val dateString = date.toString()
        val token = "Bearer " + getTokenFromSession()
        Log.d("fetchMedicationChecklist", "Fetching checklist for date: $dateString")

        RetrofitClient.retrofitService.getMedicationChecklist(dateString, token).enqueue(object :
            Callback<MedicationChecklistResponse> {
            override fun onResponse(
                call: Call<MedicationChecklistResponse>,
                response: Response<MedicationChecklistResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { fetchedResponse ->
                        Log.d("fetchMedicationChecklist", "Received response for date: $dateString with ${fetchedResponse.checklist.size} items")
                        medicationId = fetchedResponse.medicationId

                        checklistItems.clear()
                        checklistItems.addAll(fetchedResponse.checklist.filter {
                            it.goalTime.startsWith(dateString)
                        })
                        Log.d("fetchMedicationChecklist", "Filtered and added ${checklistItems.size} items to the list")
                        medicineAdapter.notifyDataSetChanged()
                        Log.d("fetchMedicationChecklist", "Adapter notified of data set change")

                        instructionTextView.visibility =
                            if (checklistItems.isEmpty()) View.GONE else View.VISIBLE
                        Log.d("fetchMedicationChecklist", "Instruction text view visibility set to ${if (checklistItems.isEmpty()) "GONE" else "VISIBLE"}")
                    }
                } else {
                    Log.e("fetchMedicationChecklist", "Failed to load checklist. Response code: ${response.code()}, message: ${response.message()}")
                    Toast.makeText(
                        context,
                        "Failed to load medication checklist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MedicationChecklistResponse>, t: Throwable) {
                Log.e("fetchMedicationChecklist", "Error fetching checklist: ${t.message}")
                Toast.makeText(
                    context,
                    "Error fetching checklist: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    // applyStateChangeToMedicineItem()에서 로그 추가
    fun applyStateChangeToMedicineItem(medicineItemId: Int, newState: State) {
        Log.d("applyStateChange", "Received newState: $newState for item ID: $medicineItemId")

        val itemIndex = checklistItems.indexOfFirst { it.id == medicineItemId }
        if (itemIndex != -1) {
            checklistItems[itemIndex] = checklistItems[itemIndex].copy(state = newState)
            Log.d("applyStateChange", "Updated item: ${checklistItems[itemIndex]}")
            medicineAdapter.updateChecklist(ArrayList(checklistItems))
            Log.d("applyStateChange", "Adapter updated")
        } else {
            Log.d("applyStateChange", "Item not found")
            fetchMedicationChecklist(selectedDate!!)
        }
    }



    private fun initWeekCalendarView() {
        weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.app_week_cal_selected_date)
                } else {
                    container.textView.setBackgroundResource(0)
                }

                container.textView.setOnClickListener { onDateSelected(data.date) }
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

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.weekcalendarDayText)
    }

    inner class MedicineAdapter(
        private var checklist: MutableList<ChecklistItem>,
        private val onItemClicked: (ChecklistItem) -> Unit
    ) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

        inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val medicineTime: TextView = itemView.findViewById(R.id.medicine_time)
            val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
            val medicineCheckbox: CheckBox = itemView.findViewById(R.id.medicine_checkbox)

            fun bind(item: ChecklistItem) {
                medicineTime.text = item.time
                medicineName.text = item.medicationName
                medicineCheckbox.isChecked = item.state == State.복용 || item.state == State.미복용

                updateViewBasedOnState(item)

                itemView.setOnClickListener { onItemClicked(item) }

                medicineCheckbox.setOnClickListener {
                    if (item.state == State.미복용 || item.state == State.복용) {
                        item.state = State.상태없음
                        applyStateChangeToMedicineItem(item.id, item.state)  // 상태를 변경하고 UI 갱신
                        updateMedicineStatus(item.id, State.상태없음, item.goalTime)  // 서버에 상태 업데이트
                        notifyItemChanged(adapterPosition)  // 아이템 업데이트
                        Toast.makeText(itemView.context, "취소되었습니다.", Toast.LENGTH_SHORT).show()

                    }
                }


                medicineCheckbox.isClickable = item.state != State.상태없음
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

                Log.d("b", "Updating status for ID: $checklistItemId to state: $newState at time: $time")

                RetrofitClient.retrofitService.updateMedicineStatus(checklistItemId, "Bearer $token", requestBody).enqueue(object :
                    Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Log.d("updateMedicineStatus", "Status updated successfully for ID: $checklistItemId to state: $newState")
                        } else {
                            Log.e("updateMedicineStatus", "Failed to update status for ID: $checklistItemId to state: $newState. Response code: ${response.code()}, message: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("updateMedicineStatus", "Error updating status for ID: $checklistItemId to state: $newState", t)
                    }
                })
            }


            private fun updateViewBasedOnState(item: ChecklistItem) {
                when (item.state) {
                    State.복용, State.미복용 -> {
                        applyStrikeThrough(medicineName, medicineTime, true)
                        itemView.setBackgroundResource(R.drawable.medicine_background)
                    }

                    State.상태없음 -> {
                        applyStrikeThrough(medicineName, medicineTime, false)

                        // Set the background based on the goal time and current time
                        val goalTime = LocalDateTime.parse(
                            item.goalTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                        val currentTime = LocalDateTime.now()

                        if (goalTime.isBefore(currentTime)) {
                            // If the goal time is before the current time, set the background to red
                            itemView.setBackgroundResource(R.drawable.background_red)
                        } else {
                            // If the goal time is after the current time, set the background to yellow
                            itemView.setBackgroundResource(R.drawable.background_yellow)
                        }
                    }
                }
            }

            private fun applyStrikeThrough(
                medicineName: TextView,
                medicineTime: TextView,
                isComplete: Boolean
            ) {
                if (isComplete) {
                    medicineName.paintFlags = medicineName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    medicineTime.paintFlags = medicineTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    medicineName.paintFlags =
                        medicineName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    medicineTime.paintFlags =
                        medicineTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_medicine_checklist_item, parent, false)
            return MedicineViewHolder(view)
        }

        override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
            holder.bind(checklist[position])
        }

        fun updateItem(position: Int, updatedItem: ChecklistItem) {
            checklist[position] = updatedItem
            notifyItemChanged(position)
        }

        override fun getItemCount(): Int = checklist.size

        fun updateChecklist(newChecklist: MutableList<ChecklistItem>) {
            checklist.clear()
            checklist.addAll(newChecklist)
            notifyDataSetChanged() // 전체 리스트 갱신
        }

    }
}