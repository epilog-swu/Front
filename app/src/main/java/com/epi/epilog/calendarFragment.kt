package com.epi.epilog

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.epi.epilog.databinding.CalendarMonthYearBinding
import com.epi.epilog.databinding.EpiDialogCustomBinding
import com.epi.epilog.databinding.FragmentCalendarBinding
import com.epi.epilog.databinding.MainCalendarBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarFragment : Fragment() {
    private var _binding: MainCalendarBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = MainCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            //카드뷰 버튼 백그라운드 설정
            val buttonBackgroundColor = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
                intArrayOf(Color.WHITE, Color.TRANSPARENT)
            )

            calendarButton.setCardBackgroundColor(buttonBackgroundColor)
            graphButton.setCardBackgroundColor(buttonBackgroundColor)

            viewPager.adapter = ViewPagerAdapter(childFragmentManager, lifecycle)
            viewPager.isUserInputEnabled = false

            calendarButton.setOnClickListener {
                calendarButton.isSelected = true
                graphButton.isSelected = false

                viewPager.currentItem = 0
            }

            graphButton.setOnClickListener {
                calendarButton.isSelected = false
                graphButton.isSelected = true

                viewPager.currentItem = 1
            }

            calendarButton.performClick()
        }
    }
}

private class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CalendarPage()
            else -> GraphPage()
        }
    }
}

