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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R
import com.epi.epilog.api.ApiResponse
import com.epi.epilog.api.ChecklistItem
import com.epi.epilog.api.MedicationChecklistResponse
import com.epi.epilog.api.MedicationStatusUpdateRequest
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.State
import com.epi.epilog.signup.LoginActivity
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MedicineChecklistFragment : Fragment() {

    private var selectedDate: LocalDate? = LocalDate.now()
    private lateinit var weekCalendarView: WeekCalendarView
    private var medicationId: Int? = null
    private var checklistItems: MutableList<ChecklistItem> = mutableListOf()
    private lateinit var instructionTextView: TextView
    internal lateinit var recyclerView: RecyclerView // 'internal'로 수정하여 외부에서 접근 가능하도록 변경

    private lateinit var medicineAdapter: MedicineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine_checklist, container, false)

        // "기존 약 관리하기" 버튼 클릭 리스너에 토큰 유효성 검사 추가
        view.findViewById<Button>(R.id.add_existing_medicine_button).setOnClickListener {
            validateToken(
                onSuccess = { // 토큰이 유효할 경우 실행
                    medicationId?.let {
                        val intent = Intent(context, MedicineDetailActivity::class.java)
                        intent.putExtra("medicationId", it)
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(context, "No medication ID found.", Toast.LENGTH_SHORT).show()
                        Log.e("MedicineChecklistFragment", "medicationId is null when trying to open MedicineDetailActivity")
                    }
                },
                onFailure = { // 토큰이 만료되었을 경우 로그인 페이지로 리다이렉트
                    redirectToLogin()
                }
            )
        }

        // "약 추가하기" 버튼 클릭 리스너에 토큰 유효성 검사 추가
        view.findViewById<Button>(R.id.add_medicine_button).setOnClickListener {
            validateToken(
                onSuccess = { // 토큰이 유효할 경우 실행
                    startActivity(Intent(context, MedicineAddModifyActivity::class.java))
                },
                onFailure = { // 토큰이 만료되었을 경우 로그인 페이지로 리다이렉트
                    redirectToLogin()
                }
            )
        }

        instructionTextView = view.findViewById(R.id.instruction_text_view)

        recyclerView = view.findViewById(R.id.medicine_content_layout)
        recyclerView.layoutManager = LinearLayoutManager(context)
        medicineAdapter = MedicineAdapter(checklistItems) { item ->
            val bottomSheetFragment = MedicineBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt("checklist_item_id", item.id)
                    putString("goal_time", item.goalTime)
                    putString("selected_date", selectedDate.toString())
                }
            }
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
        recyclerView.adapter = medicineAdapter

        return view
    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        validateToken(
            onSuccess = { // 토큰이 유효할 경우 초기 로직 실행
                selectDate(LocalDate.now())
            },
            onFailure = { // 토큰이 만료되었을 경우 로그인 페이지로 리다이렉트
                redirectToLogin()
            }
        )

        weekCalendarView = view.findViewById(R.id.meal_calendarView)
        initWeekCalendarView()
    }

    // 토큰 유효성 검사 함수
    private fun validateToken(onSuccess: () -> Unit, onFailure: () -> Unit) {
        val token = getTokenFromSession()
        if (token.isEmpty()) {
            redirectToLogin() // 토큰이 비어 있으면 바로 리다이렉트
            return
        }
        RetrofitClient.retrofitService.testApi("Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess() // 토큰이 유효할 때
                } else if (response.code() == 403) {
                    onFailure() // 토큰이 만료되었을 때
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                onFailure() // 네트워크 오류 시에도 로그인 페이지로 리다이렉트
            }
        })
    }

    // 로그인 페이지로 리다이렉트하는 함수
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
        selectedDate = date
        fetchMedicationChecklist(date)
    }

    fun onDateSelected(date: LocalDate) {
        val currentSelection = selectedDate
        selectedDate = date

        weekCalendarView.notifyDateChanged(date)
        currentSelection?.let {
            if (it != date) weekCalendarView.notifyDateChanged(it)
        }

        Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()
        selectDate(date)
    }

    private fun fetchMedicationChecklist(date: LocalDate) {
        val dateString = date.toString()
        val token = "Bearer " + getTokenFromSession()

        RetrofitClient.retrofitService.getMedicationChecklist(dateString, token).enqueue(object :
            Callback<MedicationChecklistResponse> {
            override fun onResponse(
                call: Call<MedicationChecklistResponse>,
                response: Response<MedicationChecklistResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { fetchedResponse ->
                        medicationId = fetchedResponse.medicationId

                        checklistItems.clear()
                        checklistItems.addAll(fetchedResponse.checklist.filter {
                            it.goalTime.startsWith(dateString)
                        })
                        medicineAdapter.updateChecklist(ArrayList(checklistItems))

                        instructionTextView.visibility =
                            if (checklistItems.isEmpty()) View.GONE else View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Failed to load medication checklist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MedicationChecklistResponse>, t: Throwable) {
                Toast.makeText(
                    context,
                    "Error fetching checklist: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun applyStateChangeToMedicineItem(medicineItemId: Int, newState: State) {
        val itemIndex = checklistItems.indexOfFirst { it.id == medicineItemId }
        if (itemIndex != -1) {
            checklistItems[itemIndex] = checklistItems[itemIndex].copy(state = newState)
            medicineAdapter.updateChecklist(ArrayList(checklistItems))
        } else {
            fetchMedicationChecklist(selectedDate!!)
        }
    }

    private fun initWeekCalendarView() {
        weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                Log.d("WeekDayDebug", "WeekDay date: ${data.date}")

                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.app_week_cal_selected_date)
                } else {
                    container.textView.setBackgroundResource(0)
                }

                container.textView.setOnClickListener { onDateSelected(data.date) }
            }
        }

        val currentDate = LocalDate.now()

        // 유효 날짜 계산 함수
        fun getValidDate(baseDate: LocalDate, monthsToAdd: Long, day: Int): LocalDate {
            val targetDate = baseDate.plusMonths(monthsToAdd)
            val yearMonth = YearMonth.of(targetDate.year, targetDate.monthValue)
            val validDay = if (day in 1..yearMonth.lengthOfMonth()) day else yearMonth.lengthOfMonth()
            return LocalDate.of(targetDate.year, targetDate.monthValue, validDay)
        }

        // 시작 날짜와 종료 날짜 계산
        val startDate = getValidDate(currentDate, -3, 1) // 유효한 첫 날짜
        val endDate = getValidDate(currentDate, 3, currentDate.lengthOfMonth()) // 유효한 마지막 날짜

        try {
            // 유효성 검증
            val testDateRange = generateSequence(startDate) { it.plusDays(1) }
                .takeWhile { it <= endDate }
                .toList()
            Log.d("DateRangeValidation", "Generated dates are valid: ${testDateRange.size} days")

            // weekCalendarView 설정
            weekCalendarView.setup(startDate, endDate, DayOfWeek.SUNDAY)
            weekCalendarView.scrollToWeek(currentDate)
        } catch (e: Exception) {
            Log.e("DateValidationError", "Invalid date in setup range: ${e.message}", e)
        }
        weekCalendarView.setup(startDate, endDate, DayOfWeek.SUNDAY)
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
                        applyStateChangeToMedicineItem(item.id, item.state)
                        updateMedicineStatus(item.id, State.상태없음, item.goalTime)
                        notifyItemChanged(adapterPosition)
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

                RetrofitClient.retrofitService.updateMedicineStatus(checklistItemId, "Bearer $token", requestBody).enqueue(object :
                    Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Log.d("updateMedicineStatus", "Status updated successfully for ID: $checklistItemId")
                        } else {
                            Log.e("updateMedicineStatus", "Failed to update status. Response code: ${response.code()}, message: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("updateMedicineStatus", "Error updating status for ID: $checklistItemId", t)
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
                        val goalTime = LocalDateTime.parse(
                            item.goalTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                        val currentTime = LocalDateTime.now()

                        itemView.setBackgroundResource(
                            if (goalTime.isBefore(currentTime)) R.drawable.background_red
                            else R.drawable.background_yellow
                        )
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
                    medicineName.paintFlags = medicineName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    medicineTime.paintFlags = medicineTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
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

        override fun getItemCount(): Int = checklist.size

        fun updateChecklist(newChecklist: MutableList<ChecklistItem>) {
            checklist.clear()
            checklist.addAll(newChecklist)
            notifyDataSetChanged()
        }
    }
}
