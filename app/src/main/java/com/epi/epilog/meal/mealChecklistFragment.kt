package com.epi.epilog.meal

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
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

    private var selectedDate: LocalDate? = LocalDate.now() // 기본적으로 오늘 날짜를 선택
    private lateinit var weekCalendarView: WeekCalendarView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_meal_checklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //TODO: 워치에 알람보내는 기능 추가 후 레이아웃 적용
//        super.onViewCreated(view, savedInstanceState)
//        val alarmTextView = view.findViewById<TextView>(R.id.alarm_text_view)
//        val text = "워치에 알람보내기"
//        val spannableString = SpannableString(text)
//        spannableString.setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        alarmTextView.text = spannableString

        weekCalendarView = view.findViewById(R.id.meal_calendarView)
        if (weekCalendarView == null) {
            Log.e("MealChecklistFragment", "weekCalendarView is null")
            Toast.makeText(context, "Calendar View 초기화 실패", Toast.LENGTH_SHORT).show()
            return
        }

        initWeekCalendarView(view)
    }

    private fun initWeekCalendarView(view: View) {
        Log.d("MealChecklistFragment", "initWeekCalendarView 시작")

        weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                // 오늘 날짜에 대해 기본적으로 보라색 배경 적용
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

        // 선택된 날짜와 이전에 선택된 날짜를 갱신하여 배경을 업데이트
        weekCalendarView.notifyDateChanged(date)
        if (currentSelection != null && currentSelection != date) {
            weekCalendarView.notifyDateChanged(currentSelection)
        }

        Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()

    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.weekcalendarDayText)
    }
}

