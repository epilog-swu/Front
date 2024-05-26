package com.epi.epilog.presentation

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import com.epi.epilog.R
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class CalendarInitializer(
    private val context: Context,
    private val weekCalendarView: WeekCalendarView,
    private val onDateSelectedCallback: (LocalDate) -> Unit
) {

    private var selectedDate: LocalDate? = null

    fun getSelectedDate(): LocalDate? {
        return selectedDate
    }

    fun initWeekCalendarView() {
        weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: WeekDay) {

                container.textView.text = data.date.dayOfMonth.toString()
                container.textView.setOnClickListener {
                    onDateSelected(data.date)
                }

                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.selected_date)
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

        val titlesContainer = (context as MainActivity).findViewById<ViewGroup>(R.id.titlesContainer)
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
                textView.text = title
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6f)

                val layoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginStart = 5
                layoutParams.topMargin = 5
                textView.layoutParams = layoutParams
            }
    }

    private fun onDateSelected(date: LocalDate) {
        val currentSelection = selectedDate
        if (currentSelection == date) {
            selectedDate = null
        } else {
            selectedDate = date
        }

        weekCalendarView.notifyDateChanged(date)
        if (currentSelection != null) {
            weekCalendarView.notifyDateChanged(currentSelection)
        }

        Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()
        onDateSelectedCallback(date)
    }
}
