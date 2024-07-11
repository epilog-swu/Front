package com.epi.epilog

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_calendar, container, false)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val titlesContainer = view.findViewById<ViewGroup>(R.id.MonthYear)
        val monthYearTextView = titlesContainer.findViewById<TextView>(R.id.calendarMonthYearText)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)  // Adjust as needed
        val endMonth = currentMonth.plusMonths(100)  // Adjust as needed
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library

        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)


        // MonthScrollListener를 통해 년/월 정보 업데이트
        calendarView.monthScrollListener = { month ->
            val yearMonth = month.yearMonth
            val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.getDefault())
            monthYearTextView.text = yearMonth.format(formatter)
        }



        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        // Setting the DayBinder
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                // Bind your data here
                container.textView.text = day.date.dayOfMonth.toString()
            }
        }

        // MonthHeaderBinder를 통해 년, 월 정보 바인딩
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                val daysOfWeek = daysOfWeek(firstDayOfWeek = firstDayOfWeekFromLocale())
                container.textViewSunday.text = daysOfWeek[0].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                container.textViewMonday.text = daysOfWeek[1].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                container.textViewTuesday.text = daysOfWeek[2].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                container.textViewWednesday.text = daysOfWeek[3].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                container.textViewThursday.text = daysOfWeek[4].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                container.textViewFriday.text = daysOfWeek[5].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                container.textViewSaturday.text = daysOfWeek[6].getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
            }


        return view
    }
}

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarDayText)
}

class MonthViewContainer(view: View) : ViewContainer(view) {
    val textViewSunday: TextView = view.findViewById(R.id.sunday)
    val textViewMonday: TextView = view.findViewById(R.id.monday)
    val textViewTuesday: TextView = view.findViewById(R.id.tuesday)
    val textViewWednesday: TextView = view.findViewById(R.id.wednesday)
    val textViewThursday: TextView = view.findViewById(R.id.thursday)
    val textViewFriday: TextView = view.findViewById(R.id.friday)
    val textViewSaturday: TextView = view.findViewById(R.id.saturday)
}
