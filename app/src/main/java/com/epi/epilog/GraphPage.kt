package com.epi.epilog

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
import com.epi.epilog.databinding.FragmentGraphBinding
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

class GraphPage : Fragment() {
    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: LocalDate? = null
    private lateinit var weekCalendarView: WeekCalendarView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weekCalendarView = view.findViewById(R.id.graph_calendarview)
        if (weekCalendarView == null) {
            Log.e("MealChecklistFragment", "weekCalendarView is null")
            Toast.makeText(context, "Calendar View 초기화 실패", Toast.LENGTH_SHORT).show()
            return
        }

        initWeekCalendarView(view)
        //initClickListeners(view)
    }

    private fun initWeekCalendarView(view: View) {
        Log.d("MedicineChecklistFragment", "initWeekCalendarView 시작")

        setupDayBinder()
        setupCalendarView()
        setupDayTitles(view)

        Log.d("MedicineChecklistFragment", "initWeekCalendarView 끝")
    }

    private fun setupDayBinder() {
        weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()
                container.textView.setOnClickListener {
                    onDateSelected(data.date)
                }
                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.calendar_selectday_bg)
                } else {
                    container.textView.setBackgroundResource(0)
                }
            }
        }
    }

    private fun setupCalendarView() {
        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        val startDate = currentMonth.minusMonths(100).atStartOfMonth()
        val endDate = currentMonth.plusMonths(100).atEndOfMonth()
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)

        weekCalendarView.setup(startDate, endDate, daysOfWeek.first())
        weekCalendarView.scrollToWeek(currentDate)
    }

    private fun setupDayTitles(view: View) {
        val titlesContainer = view.findViewById<ViewGroup>(R.id.app_calendar_day_titles_container)
        titlesContainer?.let { container ->
            val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)
            container.children.toList().map { it as TextView }.forEachIndexed { index, textView ->
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
        } ?: Log.e("MedicineChecklistFragment", "titlesContainer is null")
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

        // Show toast message with the selected date
        Toast.makeText(context, "Selected date: $date", Toast.LENGTH_SHORT).show()

        // Fetch and display medication checklist for the selected date
        //fetchMedicationChecklist(date)
    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }


}