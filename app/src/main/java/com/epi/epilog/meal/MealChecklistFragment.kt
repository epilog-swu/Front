package com.epi.epilog.meal

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
import com.epi.epilog.api.MealChecklistItem
import com.epi.epilog.api.MealChecklistResponse
import com.epi.epilog.api.MedicationStatusUpdateRequest
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.MealState
import com.epi.epilog.api.MealStatusUpdateRequest
import com.epi.epilog.api.State
import com.epi.epilog.medicine.MealBottomSheetFragment
import com.epi.epilog.medicine.MedicineAddModifyActivity
import com.epi.epilog.medicine.MedicineBottomSheetFragment
import com.epi.epilog.medicine.MedicineDetailActivity
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

class MealChecklistFragment : Fragment() {

    private lateinit var weekCalendarView: WeekCalendarView
    private var selectedDate: LocalDate? = LocalDate.now()
    private var checklistItems: MutableList<MealChecklistItem> = mutableListOf()
    private lateinit var instructionTextView: TextView
    lateinit var recyclerView: RecyclerView
    private lateinit var mealAdapter: MealAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_meal_checklist, container, false)

        Log.d("MealChecklistFragment", "Layout inflated successfully.")


        instructionTextView = view.findViewById(R.id.instruction_text_view)

        recyclerView = view.findViewById(R.id.meal_content_layout)
        recyclerView.layoutManager = LinearLayoutManager(context)
        mealAdapter = MealAdapter(checklistItems) { item ->
            val bottomSheetFragment = MealBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt("checklist_item_id", item.id)
                    putString("goal_time", item.goalTime)
                    putString("selected_date", selectedDate.toString())
                }
            }
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
        recyclerView.adapter = mealAdapter

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

        // "식사 관리 하기" 버튼 클릭 리스너에 토큰 유효성 검사 추가
        view.findViewById<Button>(R.id.manage_mealtime_button).setOnClickListener {
            validateToken(
                onSuccess = { // 토큰이 유효할 경우 실행
                    //TODO : 여기 실행 클래스명 바껴야함
                    startActivity(Intent(context, MealManageTimeActivity::class.java))
                },
                onFailure = { // 토큰이 만료되었을 경우 로그인 페이지로 리다이렉트
                    redirectToLogin()
                }
            )
        }

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

    private fun fetchMealChecklist(date: LocalDate) {
        val dateString = date.toString()
        val token = "Bearer " + getTokenFromSession()
        Log.d("FetchMealChecklist","token : $token")

        RetrofitClient.retrofitService.getMealChecklist(dateString, token).enqueue(object :
            Callback<MealChecklistResponse> {
            override fun onResponse(
                call: Call<MealChecklistResponse>,
                response: Response<MealChecklistResponse>
            ) {

                // 응답 상태 코드 로그 출력
//                Log.d("FetchMealChecklist", "Response code: ${response.code()}")
//                Log.d("FetchMealChecklist", "Raw response: ${response.raw()}")
//                Log.d("FetchMealChecklist", "Response body: ${response.body()}")
//                Log.d("FetchMealChecklist", "Error body: ${response.errorBody()?.string()}")

                // 응답 본문 확인
                if (response.body() == null) {
                    Log.e("FetchMealChecklist", "Empty response body")
                    Toast.makeText(
                        context,
                        "해당 날짜 체크리스트가 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }


                if (response.isSuccessful) {
                    response.body()?.let { fetchedResponse ->

                        // 성공 로그 추가
                        Log.d("FetchMealChecklist", "Successfully fetched meal checklist: $fetchedResponse")

                        checklistItems.clear()
                        checklistItems.addAll(fetchedResponse.checklist.filter {
                            it.goalTime.startsWith(dateString)
                        })
                        mealAdapter.updateChecklist(ArrayList(checklistItems))


                        // RecyclerView와 안내문 상태 업데이트
                        if (checklistItems.isEmpty()) {
                            Log.d("FetchMealChecklist", "No items available. Showing instructionTextView.")
                            instructionTextView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            Log.d("FetchMealChecklist", "Items available. Showing RecyclerView.")
                            instructionTextView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Failed to load meal checklist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MealChecklistResponse>, t: Throwable) {

                Log.e("FetchMealChecklist", "Error fetching checklist: ${t.message}")
                // 요청 정보 출력
                Log.e("FetchMealChecklist", "Request info: ${call.request().toString()}")


                Toast.makeText(
                    context,
                    "Error fetching checklist: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
        })
    }

    fun applyStateChangeToMealItem(mealItemId: Int, newState: MealState) {
        val itemIndex = checklistItems.indexOfFirst { it.id == mealItemId }
        if (itemIndex != -1) {
            checklistItems[itemIndex] = checklistItems[itemIndex].copy(state = newState)
            mealAdapter.updateChecklist(ArrayList(checklistItems))
        } else {
            fetchMealChecklist(selectedDate!!)
        }
    }

    private fun getTokenFromSession(): String {
        val sharedPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("AuthToken", "") ?: ""
    }

    // 로그인 페이지로 리다이렉트하는 함수
    private fun redirectToLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun selectDate(date: LocalDate) {
        selectedDate = date
        fetchMealChecklist(date)
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
            Log.e("WeekCalendarError", "Error in calendar setup: ${e.message}", e)
        }
    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.weekcalendarDayText)
    }


    inner class MealAdapter(
        private var checklist: MutableList<MealChecklistItem> = mutableListOf(),
        private val onItemClicked: (MealChecklistItem) -> Unit
    ) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

        private var isClickable: Boolean = false

        inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val mealTime: TextView = itemView.findViewById(com.epi.epilog.R.id.meal_time)
            val mealName: TextView = itemView.findViewById(com.epi.epilog.R.id.meal_name)
            val mealCheckBox: CheckBox = itemView.findViewById(com.epi.epilog.R.id.meal_checkbox)

            fun bind(item: MealChecklistItem) {
                mealName.text = item.mealType
                mealTime.text = item.time
                mealCheckBox.isChecked = item.state == MealState.식사함 || item.state == MealState.건너뜀

                updateViewBasedOnState(item)

                itemView.setOnClickListener { onItemClicked(item) }

                //TODO : 체크박스 클릭 리스너
                mealCheckBox.setOnClickListener {
                    if (item.state == MealState.건너뜀 || item.state == MealState.식사함) {
                        item.state = MealState.상태없음
                        applyStateChangeToMealItem(item.id, item.state)
                        updateMealStatus(item.id, MealState.상태없음, item.goalTime)
                        notifyItemChanged(adapterPosition)
                        Toast.makeText(itemView.context, "취소되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                mealAdapter.isClickable = item.state != MealState.상태없음
            }

            private fun updateMealStatus(checklistItemId: Int, newState: MealState, time: String) {
                val token = getTokenFromSession()

                if (token.isNullOrBlank()) {
                    Log.e("MealChecklistFragment", "Token is missing or invalid")
                    return
                }

                val requestBody = MealStatusUpdateRequest(
                    time = time,
                    status = newState
                )

                RetrofitClient.retrofitService.updateMealStatus(checklistItemId, "Bearer $token", requestBody).enqueue(object :
                    Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Log.d("updateMealStatus", "Status updated successfully for ID: $checklistItemId")
                        } else {
                            Log.e("updateMealStatus", "Failed to update status. Response code: ${response.code()}, message: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("updateMealStatus", "Error updating status for ID: $checklistItemId", t)
                    }
                })
            }



            private fun updateViewBasedOnState(item: MealChecklistItem) {
                when (item.state) {
                    MealState.식사함, MealState.건너뜀 -> {
                        applyStrikeThrough(mealTime,true)
                        itemView.setBackgroundResource(R.drawable.medicine_background)
                    }

                    MealState.상태없음 -> {
                        applyStrikeThrough(mealTime, false)
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
                mealTime: TextView,
                isComplete: Boolean
            ) {
                if (isComplete) {
                    mealTime.paintFlags = mealTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    mealTime.paintFlags = mealTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            }


        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MealAdapter.MealViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_meal_checklist_item, parent, false)
            return MealViewHolder(view)
        }

        override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
            Log.d("MealAdapter", "Binding item at position: $position -> ${checklist[position]}")
            holder.bind(checklist[position])
        }

        override fun getItemCount(): Int = checklist.size

        fun updateChecklist(newChecklist: MutableList<MealChecklistItem>) {
            Log.d("MealAdapter", "Updating checklist. New size: ${newChecklist.size}")
            checklist.clear()
            checklist.addAll(newChecklist)
            Log.d("MealAdapter", "Checklist after update: $checklist")
            notifyDataSetChanged()
        }

        }


}
