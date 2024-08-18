package com.epi.epilog.medicine

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.epi.epilog.LoginActivity
import com.epi.epilog.R
import com.epi.epilog.api.ChecklistItem
import com.epi.epilog.api.MedicationChecklistResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.meal.MealChecklistFragment
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
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
import java.util.Locale


class MedicineChecklistFragment : Fragment() {

    private var selectedDate: LocalDate? = LocalDate.now()
    private var medicationId: Int? = null
    private lateinit var weekCalendarView: WeekCalendarView

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
        val alarmTextView = view.findViewById<TextView>(R.id.alarm_text_view)
        val text = "워치에 알람보내기"
        val spannableString = SpannableString(text)
        spannableString.setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        alarmTextView.text = spannableString

        validateToken()

        weekCalendarView = view.findViewById(R.id.meal_calendarView)
        if (weekCalendarView == null) {
            Log.e("MealChecklistFragment", "weekCalendarView is null")
            Toast.makeText(context, "Calendar View 초기화 실패", Toast.LENGTH_SHORT).show()
            return
        }

        initWeekCalendarView(view)
    }

    private fun validateToken() {
        val token = getTokenFromSession()
        RetrofitClient.retrofitService.testApi("Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    selectedDate?.let {
                        selectDate(it) // 초기화 시 오늘 날짜 데이터 로드
                    }
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

    private fun getTokenFromSession(): String {
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
                        medicationId = fetchedResponse.medicationId
                        updateChecklistUI(fetchedResponse.checklist)
                        Log.d("MedicineChecklistFragment", "Medication ID: $medicationId")
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

            updateItemViewBackground(item.isComplete, now, goalTime, itemView)

            itemView.setOnClickListener {
                val bottomSheetFragment = MedicineBottomSheetFragment()
                bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
            }

            medicineCheckbox.setOnCheckedChangeListener { _, isChecked ->
                item.isComplete = isChecked
                updateItemViewBackground(isChecked, now, goalTime, itemView)
                applyStrikeThrough(medicineName, medicineTime, isChecked)
            }

            medicineContentLayout?.addView(itemView)
        }
    }

    private fun updateItemViewBackground(isComplete: Boolean, now: LocalDateTime, goalTime: LocalDateTime, itemView: View) {
        if (isComplete) {
            itemView.setBackgroundResource(R.drawable.medicine_background) // 체크 시 보라색 배경
        } else {
            itemView.background = when {
                now.isAfter(goalTime) -> ContextCompat.getDrawable(requireContext(), R.drawable.background_red) // 목표 시간 < 현재 시간
                else -> ContextCompat.getDrawable(requireContext(), R.drawable.background_yellow) // 목표 시간 > 현재 시간
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



    private fun initWeekCalendarView(view: View) {
        Log.d("MealChecklistFragment", "initWeekCalendarView 시작")

        weekCalendarView.dayBinder = object :
            WeekDayBinder<MedicineChecklistFragment.DayViewContainer> {
            override fun create(view: View) = MedicineChecklistFragment.DayViewContainer(view)

            override fun bind(container: MedicineChecklistFragment.DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.app_week_cal_selected_date)
                } else {
                    container.textView.setBackgroundResource(0) // 기본 배경
                }

                container.textView.setOnClickListener {
                    onDateSelected(data.date)
                }
            }
        }

        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        val startDate = currentMonth.minusMonths(100).atStartOfMonth()
        val endDate = currentMonth.plusMonths(100).atEndOfMonth()
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)

        weekCalendarView.setup(startDate, endDate, daysOfWeek.first())
        weekCalendarView.scrollToWeek(currentDate)

        val titlesContainer = view.findViewById<ViewGroup>(R.id.app_calendar_day_titles_container)
        titlesContainer?.children?.map { it as TextView }?.forEachIndexed { index, textView ->
            val dayOfWeek = daysOfWeek[index]
            val title = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
            textView.text = title
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6f)

            val layoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginStart = 5
            layoutParams.topMargin = 5
            textView.layoutParams = layoutParams
        }

        Log.d("MealChecklistFragment", "initWeekCalendarView 끝")
    }

    private fun onDateSelected(date: LocalDate) {
        val currentSelection = selectedDate
        selectedDate = date

        weekCalendarView.notifyDateChanged(date)
        if (currentSelection != null && currentSelection != date) {
            weekCalendarView.notifyDateChanged(currentSelection)
        }

        Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()

        selectDate(date) // 선택된 날짜의 체크리스트 데이터 로드
    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.weekcalendarDayText)
    }
}
