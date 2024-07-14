package com.epi.epilog

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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

        // 커스텀 대화상자 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.epi_dialog_custom, null)

        calendarView = view.findViewById(R.id.calendarView)
        val titlesContainer = view.findViewById<ViewGroup>(R.id.MonthYear)
        val monthYearTextView = titlesContainer.findViewById<TextView>(R.id.calendarMonthYearText)
        val pdfCancleButton = view.findViewById<View>(R.id.pdf_cancel_btn)
        val pdfRangeButton = view.findViewById<View>(R.id.pdf_range_btn)
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

        // MonthHeaderBinder를 통해 요일 정보 바인딩
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                val daysOfWeek = daysOfWeek(firstDayOfWeek = firstDayOfWeekFromLocale())
                container.textViewSunday.text =
                    daysOfWeek[0].getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                container.textViewMonday.text =
                    daysOfWeek[1].getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                container.textViewTuesday.text =
                    daysOfWeek[2].getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                container.textViewWednesday.text =
                    daysOfWeek[3].getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                container.textViewThursday.text =
                    daysOfWeek[4].getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                container.textViewFriday.text =
                    daysOfWeek[5].getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                container.textViewSaturday.text =
                    daysOfWeek[6].getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            }
        }

        pdfRangeButton.setOnClickListener {

            // 기간선택 끝났는지 확인
            if (rangeStartDate != null && rangeEndDate != null) {
                Toast.makeText(
                    context,
                    "Selected range: $rangeStartDate to $rangeEndDate",
                    Toast.LENGTH_SHORT
                ).show()
                // Add your PDF generation logic here
            } else {
                Toast.makeText(context, "범위를 선택해주세요", Toast.LENGTH_SHORT).show()
            }

            //버튼 활성화
            if (pdfRangeButton.visibility == View.VISIBLE) {
                pdfDownloadButton.visibility = View.VISIBLE
                pdfCancleButton.visibility = View.VISIBLE
                pdfRangeButton.visibility = View.INVISIBLE
            }
        }

        pdfDownloadButton.setOnClickListener {
            // rangeStartDate와 rangeEndDate를 이용하여 날짜 범위 문자열 생성
            if (rangeStartDate != null && rangeEndDate != null) {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.getDefault())

                // 커스텀 대화상자 생성//현아 대화상자 적용, epi_dialog_custom 레이아웃으로 적용하기
                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.epi_dialog_custom, null)

                //현아 대화상자 적용!!! 여기 확인
                val dialogBuilder =
                    AlertDialog.Builder(requireContext(), R.style.RoundCornerDialogStyle)
                dialogBuilder.setView(dialogView)


                //현아 대화상자 적용 //바인딩 변수명 바꿔주기
                val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessageTV)
                val cancelButton = dialogView.findViewById<Button>(R.id.dialogCancleBtn)
                val confirmButton = dialogView.findViewById<Button>(R.id.dialogOkBtn)

                // 날짜 범위 문자열 설정
                dialogMessage.text = "$rangeStartDate 부터 $rangeEndDate \n 까지의 일지를 PDF로 변환하시겠습니까?"

                val alertDialog = dialogBuilder.create()

                cancelButton.setOnClickListener { alertDialog.dismiss() }
                confirmButton.setOnClickListener {
                    // PDF 변환 로직 추가
                    Toast.makeText(context, "PDF로 변환 중...", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }

                alertDialog.show()
            } else {
                Toast.makeText(context, "범위가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
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
