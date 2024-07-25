package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.fragment.app.Fragment
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

class MedicineChecklistFragment : Fragment() {

    private var selectedDate: LocalDate? = null
    private lateinit var weekCalendarView: WeekCalendarView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine_checklist, container, false)

        // Toolbar 설정
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize WeekCalendarView
        weekCalendarView = view.findViewById(R.id.meal_calendarView)
        initWeekCalendarView(view)

        view.findViewById<Button>(R.id.add_existing_medicine_button).setOnClickListener {
            startActivity(Intent(context, MedicineDetailActivity::class.java))
        }

        view.findViewById<Button>(R.id.add_medicine_button).setOnClickListener {
            startActivity(Intent(context, MedicineAddModifyActivity::class.java))
        }

        // Add click listener to medicine items
        addMedicineItemClickListener(view)

        return view
    }

    private fun addMedicineItemClickListener(view: View) {
        val medicineItems = listOf(
            view.findViewById<LinearLayout>(R.id.red_box),
            view.findViewById<LinearLayout>(R.id.orange_box),
            view.findViewById<LinearLayout>(R.id.purple_box)
        )

       medicineItems.forEach { item ->
          item.setOnClickListener {
              val bottomSheet = MedicineBottomSheetFragment()
               bottomSheet.show(parentFragmentManager, bottomSheet.tag)
          }
        }
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
                    container.textView.setBackgroundResource(R.drawable.app_week_cal_selected_date)
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
    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.weekcalendarDayText)
    }
}
