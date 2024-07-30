package com.epi.epilog.meal

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.epi.epilog.R
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class MealChecklistFragment : Fragment() {

    private var selectedDate: LocalDate? = null
    private lateinit var weekCalendarView: WeekCalendarView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meal_checklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weekCalendarView = view.findViewById(R.id.meal_calendarView)
        if (weekCalendarView == null) {
            Log.e("MealChecklistFragment", "weekCalendarView is null")
            Toast.makeText(context, "Calendar View 초기화 실패", Toast.LENGTH_SHORT).show()
            return
        }

        initWeekCalendarView(view)
        //initClickListeners(view)
    }

    private fun initWeekCalendarView(view: View) {
        Log.d("MealChecklistFragment", "initWeekCalendarView 시작")

        weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()
                container.textView.setOnClickListener {
                    onDateSelected(data.date)
                }
                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.app_week_cal_selected_date)
                } else {
                    container.textView.setBackgroundResource(0)
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
        if (titlesContainer == null) {
            Log.e("MealChecklistFragment", "titlesContainer is null")
        } else {
            Log.d("MealChecklistFragment", "titlesContainer is not null")
        }

        titlesContainer?.children?.map { it as TextView }?.forEachIndexed { index, textView ->
            val dayOfWeek = daysOfWeek[index]
            val title = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
            textView.text = title
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6f)

            val layoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginStart = 5
            layoutParams.topMargin = 5
            textView.layoutParams = layoutParams
        } ?: run {
            Toast.makeText(context, "Titles container not found", Toast.LENGTH_SHORT).show()
        }

        Log.d("MealChecklistFragment", "initWeekCalendarView 끝")
    }

    //클릭 리스너 모음
//    private fun initClickListeners(view: View) {
//        val calculateOnedayEnergyLayout = view.findViewById<LinearLayout>(R.id.calculate_oneday_energy_layout)
//        calculateOnedayEnergyLayout.setOnClickListener {
//            val intent = Intent(context, ActivityMealManageSex::class.java)
//            startActivity(intent)
//        }
//    }

    private fun onDateSelected(date: LocalDate) {
        val currentSelection = selectedDate
        if (currentSelection == date) {
            selectedDate = null
        } else {
            selectedDate = date
            Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()
            onDateSelectedCallback(date)
        }

        weekCalendarView.notifyDateChanged(date)
        if (currentSelection != null) {
            weekCalendarView.notifyDateChanged(currentSelection)
        }
    }

    private fun onDateSelectedCallback(date: LocalDate) {
        // Implement your callback logic here
    }

    fun getSelectedDate(): LocalDate? {
        return selectedDate
    }

    fun selectDate(date: LocalDate) {
        selectedDate = date
        weekCalendarView.notifyDateChanged(date)
        Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()
    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.weekcalendarDayText)
    }
}
