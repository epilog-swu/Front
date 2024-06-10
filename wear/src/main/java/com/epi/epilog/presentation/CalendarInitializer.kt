import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import com.epi.epilog.R
import com.epi.epilog.presentation.DayViewContainer
import com.epi.epilog.presentation.MainActivity
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
    private val context: Context, // 애플리케이션 컨텍스트
    private val weekCalendarView: WeekCalendarView, // 주간 캘린더 뷰
    private val onDateSelectedCallback: (LocalDate) -> Unit // 날짜 선택 콜백 함수
) {

    private var selectedDate: LocalDate? = null // 현재 선택된 날짜를 저장

    fun getSelectedDate(): LocalDate? {
        return selectedDate // 현재 선택된 날짜를 반환
    }

    // 주간 캘린더 뷰를 초기화하는 함수
    fun initWeekCalendarView() {
        // 주간 캘린더의 DayBinder를 설정
        weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view) // DayViewContainer 생성

            override fun bind(container: DayViewContainer, data: WeekDay) {
                // 날짜를 표시하는 텍스트뷰에 날짜를 설정
                container.textView.text = data.date.dayOfMonth.toString()
                container.textView.setOnClickListener {
                    onDateSelected(data.date) // 날짜 클릭 시 onDateSelected 함수 호출
                }

                // 선택된 날짜와 비교하여 배경 리소스를 설정
                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.selected_date)
                } else {
                    container.textView.setBackgroundResource(0)
                }
            }
        }

        // 현재 날짜 및 월을 가져옴
        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        // 시작 날짜를 100개월 전으로 설정
        val startDate = currentMonth.minusMonths(100).atStartOfMonth()
        // 종료 날짜를 100개월 후로 설정
        val endDate = currentMonth.plusMonths(100).atEndOfMonth()

        // 주의 시작 요일을 일요일로 설정
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)

        // 주간 캘린더 뷰를 설정
        weekCalendarView.setup(startDate, endDate, daysOfWeek.first())
        // 주간 캘린더 뷰를 현재 주로 스크롤
        weekCalendarView.scrollToWeek(currentDate)

        // 캘린더 헤더를 설정
        val titlesContainer = (context as MainActivity).findViewById<ViewGroup>(R.id.titlesContainer)
        titlesContainer.children
            .map { it as TextView } // 자식 뷰를 TextView로 변환
            .forEachIndexed { index, textView ->
                // 각 요일의 이름을 설정
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
                textView.text = title
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6f) // 텍스트 크기를 설정

                // 레이아웃 매개변수를 설정
                val layoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginStart = 5
                layoutParams.topMargin = 5
                textView.layoutParams = layoutParams
            }
    }

    // 날짜 선택 시 호출되는 함수
    private fun onDateSelected(date: LocalDate) {
        val currentSelection = selectedDate
        if (currentSelection == date) {
            selectedDate = null // 동일 날짜 선택 시 선택 해제
        } else {
            selectedDate = date // 새로운 날짜 선택

            // 날짜가 새롭게 선택된 경우에만 토스트 메시지를 표시
            Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()
            onDateSelectedCallback(date) // 콜백 함수 호출
        }

        // 선택된 날짜와 이전 선택된 날짜의 뷰를 갱신
        weekCalendarView.notifyDateChanged(date)
        if (currentSelection != null) {
            weekCalendarView.notifyDateChanged(currentSelection)
        }
    }
}


