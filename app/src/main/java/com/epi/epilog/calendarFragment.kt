package com.epi.epilog

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class CalendarFragment : Fragment() {

    private var rangeStartDate: LocalDate? = null
    private var rangeEndDate: LocalDate? = null
    private lateinit var calendarView: CalendarView

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

        calendarView = view.findViewById(R.id.calendarView)
        val titlesContainer = view.findViewById<ViewGroup>(R.id.MonthYear)
        val monthYearTextView = titlesContainer.findViewById<TextView>(R.id.calendarMonthYearText)
        val pdfDownloadButton = view.findViewById<View>(R.id.pdf_download_btn)

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
                container.textView.text = day.date.dayOfMonth.toString()

                // Apply original background here
                container.textView.setBackgroundResource(R.drawable.calendar_day_bg)

                if (day.position == DayPosition.MonthDate) {
                    when {
                        day.date == rangeStartDate || day.date == rangeEndDate -> {
                            container.textView.setBackgroundResource(R.drawable.calendar_selectday_bg)
                            container.textView.setTextColor(Color.BLACK)
                        }
                        rangeStartDate != null && rangeEndDate != null && (day.date > rangeStartDate && day.date < rangeEndDate) -> {
                            container.textView.setBackgroundResource(R.drawable.calendar_rangeday_bg)
                            container.textView.setTextColor(Color.BLACK)
                        }
                        else -> {
                            container.textView.setBackgroundResource(R.drawable.calendar_day_bg)
                            container.textView.setTextColor(Color.BLACK)
                        }
                    }
                    container.textView.setOnClickListener {
                        onDateSelected(day.date)
                    }
                } else {
                    container.textView.setTextColor(Color.GRAY)
                    //container.textView.setBackgroundColor(Color.TRANSPARENT)
                    container.textView.setOnClickListener(null)
                }
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

        pdfDownloadButton.setOnClickListener {
            // Show selected date range when button is clicked
            if (rangeStartDate != null && rangeEndDate != null) {
                Toast.makeText(context, "Selected range: $rangeStartDate to $rangeEndDate", Toast.LENGTH_SHORT).show()
                // Add your PDF generation logic here
            } else {
                Toast.makeText(context, "No range selected", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun onDateSelected(date: LocalDate) {
        if (rangeStartDate == null) {
            rangeStartDate = date
        } else if (rangeEndDate == null) {
            if (date.isBefore(rangeStartDate)) {
                rangeEndDate = rangeStartDate
                rangeStartDate = date
            } else {
                rangeEndDate = date
            }
        } else {
            rangeStartDate = date
            rangeEndDate = null
        }
        calendarView.notifyCalendarChanged()
    }
}

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.calendarDayText)
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
