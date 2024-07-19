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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.epi.epilog.databinding.CalendarMonthYearBinding
import com.epi.epilog.databinding.MainCalendarBinding
import com.epi.epilog.databinding.EpiDialogCustomBinding
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


    private var _binding: MainCalendarBinding? = null
    private val binding get() = _binding!!
    //기간 선택 (비)활성화 변수
    private var isDateSelectionEnabled: Boolean = false
    private var rangeStartDate: LocalDate? = null
    private var rangeEndDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = MainCalendarBinding.inflate(inflater, container, false)
        val view = binding.root

        // 커스텀 대화상자 생성
        val dialogBinding = EpiDialogCustomBinding.inflate(inflater, container, false)

        // 포함된 레이아웃의 바인딩 초기화
        val monthYearBinding = CalendarMonthYearBinding.bind(binding.MonthYear.root)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)  // Adjust as needed
        val endMonth = currentMonth.plusMonths(100)  // Adjust as needed
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library

        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)

        // MonthScrollListener를 통해 년/월 정보 업데이트
        binding.calendarView.monthScrollListener = { month ->
            val yearMonth = month.yearMonth
            val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.getDefault())
            monthYearBinding.calendarMonthYearText.text = yearMonth.format(formatter)
        }

        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        // Setting the DayBinder
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
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

                        if(isDateSelectionEnabled){
                            //바텀시트2 보이기
                        }
                        else{
                            //바텀시트1보이기
                            showBottomSheet(day.date.toString())
                        }
                    }
                } else {
                    container.textView.setTextColor(Color.GRAY)
                    //container.textView.setBackgroundColor(Color.TRANSPARENT)
                    container.textView.setOnClickListener(null)
                }
            }
        }

        // MonthHeaderBinder를 통해 요일 정보 바인딩
        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
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


        //기간선택하기를 누르면, 활성화해주고, 범위선택
        binding.pdfRangeBtn.setOnClickListener {

            isDateSelectionEnabled = true

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
            if (binding.pdfRangeBtn.visibility == View.VISIBLE) {
                binding.pdfDownloadBtn.visibility = View.VISIBLE
                binding.pdfCancelBtn.visibility = View.VISIBLE
                binding.pdfRangeBtn.visibility = View.INVISIBLE
            }
        }

        //PDF 변환하기를 누르면, 기간 선택하고 대화상자 생성
        binding.pdfDownloadBtn.setOnClickListener {
            // rangeStartDate와 rangeEndDate를 이용하여 날짜 범위 문자열 생성
            if (rangeStartDate != null && rangeEndDate != null) {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.getDefault())

                // 커스텀 대화상자 생성
                val dialogBinding = EpiDialogCustomBinding.inflate(inflater, container, false)

                // 대화상자 빌더 설정
                val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.RoundCornerDialogStyle)
                dialogBuilder.setView(dialogBinding.root)

                // 대화상자 메시지 설정
                dialogBinding.dialogMessageTV.text = "$rangeStartDate 부터 $rangeEndDate \n 까지의 일지를 PDF로 변환하시겠습니까?"

                val alertDialog = dialogBuilder.create()

                dialogBinding.dialogCancleBtn.setOnClickListener { alertDialog.dismiss() }
                dialogBinding.dialogOkBtn.setOnClickListener {
                    // PDF 변환 로직 추가
                    Toast.makeText(context, "PDF로 변환 중...", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }

                alertDialog.show()
            } else {
                Toast.makeText(context, "범위가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        //취소하기를 누르면
        binding.pdfCancelBtn.setOnClickListener {
            isDateSelectionEnabled = false

            //버튼 (비)활성화
            if (binding.pdfCancelBtn.visibility == View.VISIBLE) {
                binding.pdfDownloadBtn.visibility = View.INVISIBLE
                binding.pdfCancelBtn.visibility = View.INVISIBLE
                binding.pdfRangeBtn.visibility = View.VISIBLE
            }

            rangeStartDate = null
            rangeEndDate = null

            // CalendarView를 업데이트하여 모든 날짜의 배경을 초기화
            binding.calendarView.notifyCalendarChanged()

        }

        return view
    }


    fun setDateSelectionEnabled(enabled: Boolean) {
        isDateSelectionEnabled = enabled
    }

    private fun onDateSelected(date: LocalDate) {
        if (!isDateSelectionEnabled) {
            //Toast.makeText(context, "기간 선택이 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        else{
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
        }

        binding.calendarView.notifyCalendarChanged()
    }

    private fun showBottomSheet(date: String) {
        val bottomSheetFragment = BottomSheetFragment().apply {
            arguments = Bundle().apply {
                putString("date", date)
            }
        }
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
