package com.epi.epilog.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.epi.epilog.signup.LoginActivity
import com.epi.epilog.R
import com.epi.epilog.api.DiaryCountResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.databinding.CalendarMonthYearBinding
import com.epi.epilog.databinding.EpiDialogCustomBinding
import com.epi.epilog.databinding.FragmentCalendarBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarPage : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // 기간 선택 (비)활성화 변수
    private var isRangeSelectionEnabled: Boolean = false
    private var rangeStartDate: LocalDate? = null
    private var rangeEndDate: LocalDate? = null

    // 초기화 시 오늘 날짜를 선택하도록 selectedDate를 오늘 날짜로 설정
    val today = LocalDate.now()
    var selectedDate: LocalDate? = today

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Validate token before setting up the calendar
        validateTokenAndProceed()
    }

    private fun validateTokenAndProceed() {
        val token = getAuthToken()
        RetrofitClient.retrofitService.testApi("Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    setupCalendarView() // Proceed with calendar setup if the token is valid
                } else {
                    redirectToLogin() // Redirect to login if the token is invalid
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                redirectToLogin()
            }
        })
    }

    private fun setupCalendarView() {
        // MonthYear 레이아웃 포함
        val calendarMonthYearBinding = CalendarMonthYearBinding.bind(binding.MonthYear.root)

        // 이전 달 버튼 클릭 리스너 설정
        calendarMonthYearBinding.calendarMonthBackBtn.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let { month ->
                binding.calendarView.smoothScrollToMonth(month.yearMonth.minusMonths(1))
            }
        }

        // 다음 달 버튼 클릭 리스너 설정
        calendarMonthYearBinding.calendarMonthNextBtn.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let { month ->
                binding.calendarView.smoothScrollToMonth(month.yearMonth.plusMonths(1))
            }
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)  // Adjust as needed
        val endMonth = currentMonth.plusMonths(100)  // Adjust as needed
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library

        // MonthScrollListener를 통해 년/월 정보 업데이트
        binding.calendarView.monthScrollListener = { month ->
            val yearMonth = month.yearMonth
            val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.getDefault())
            calendarMonthYearBinding.calendarMonthYearText.text = yearMonth.format(formatter)
            fetchDiaryCounts(yearMonth.year, yearMonth.monthValue)
        }

        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()

                // 기본 배경 설정
                container.textView.setBackgroundResource(R.drawable.calendar_day_bg)

                // 현재 달에 해당하는 날짜인지 확인
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

                        day.date == selectedDate -> {
                            container.textView.setBackgroundResource(R.drawable.calendar_today_bg)
                            container.textView.setTextColor(Color.BLACK)
                        }

                        else -> {
                            val diaryCount = diaryCountsMap[day.date] ?: 0
                            when {
                                diaryCount in 1..2 -> container.textView.setBackgroundResource(R.drawable.calendar_day_count1_bg)
                                diaryCount in 3..6 -> container.textView.setBackgroundResource(R.drawable.calendar_day_count3_bg)
                                diaryCount >= 7 -> container.textView.setBackgroundResource(R.drawable.calendar_selectday_bg)
                                else -> container.textView.setBackgroundResource(R.drawable.calendar_day_bg)
                            }
                            container.textView.setTextColor(Color.BLACK)
                        }
                    }

                    // 날짜가 선택됐을 때
                    container.textView.setOnClickListener {
                        if (!isRangeSelectionEnabled) {
                            selectedDate = day.date
                            // 바텀시트1보이기
                            showBottomSheet(day.date.toString())
                            binding.calendarView.notifyCalendarChanged()
                        } else {
                            onDateSelected(day.date)
                        }
                    }
                } else {
                    container.textView.setTextColor(Color.GRAY)
                    container.textView.setOnClickListener(null)
                }
            }
        }

        // MonthHeaderBinder를 통해 요일 정보 바인딩
        binding.calendarView.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
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

        setupPdfGeneration() // Setup PDF generation buttons and logic
    }

    private fun setupPdfGeneration() {
        // 기간선택하기를 누르면, 활성화해주고, 범위선택
        binding.pdfRangeBtn.setOnClickListener {
            isRangeSelectionEnabled = true

            if (rangeStartDate != null && rangeEndDate != null) {
                Toast.makeText(
                    context,
                    "Selected range: $rangeStartDate to $rangeEndDate",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, "범위를 선택해주세요", Toast.LENGTH_SHORT).show()
            }

            if (binding.pdfRangeBtn.visibility == View.VISIBLE) {
                binding.pdfDownloadBtn.visibility = View.VISIBLE
                binding.pdfCancelBtn.visibility = View.VISIBLE
                binding.pdfRangeBtn.visibility = View.INVISIBLE
            }
        }

        // PDF 변환하기를 누르면, 기간 선택하고 대화상자 생성
        binding.pdfDownloadBtn.setOnClickListener {
            if (rangeStartDate != null && rangeEndDate != null) {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.getDefault())

                val inflater = LayoutInflater.from(requireContext())
                val dialogBinding = EpiDialogCustomBinding.inflate(inflater)

                val dialogBuilder =
                    AlertDialog.Builder(requireContext(), R.style.RoundCornerDialogStyle)
                dialogBuilder.setView(dialogBinding.root)

                dialogBinding.dialogMessageTV.text =
                    "$rangeStartDate 부터 $rangeEndDate \n 까지의 일지를 PDF로 변환하시겠습니까?"

                val alertDialog = dialogBuilder.create()

                dialogBinding.dialogCancleBtn.setOnClickListener { alertDialog.dismiss() }
                dialogBinding.dialogOkBtn.setOnClickListener {
                    val authToken = getAuthToken()
                    downloadAndSavePDF(
                        rangeStartDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        rangeEndDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        authToken
                    )
                    Toast.makeText(context, "PDF로 변환 중...", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }

                alertDialog.show()
            } else {
                Toast.makeText(context, "범위가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 취소하기를 누르면
        binding.pdfCancelBtn.setOnClickListener {
            isRangeSelectionEnabled = false

            if (binding.pdfCancelBtn.visibility == View.VISIBLE) {
                binding.pdfDownloadBtn.visibility = View.INVISIBLE
                binding.pdfCancelBtn.visibility = View.INVISIBLE
                binding.pdfRangeBtn.visibility = View.VISIBLE
            }

            rangeStartDate = null
            rangeEndDate = null

            binding.calendarView.notifyCalendarChanged()
        }
    }
    private fun downloadAndSavePDF(startDate: String, endDate: String, token: String) {
        Log.d("CalendarPage", "Start Date: $startDate")
        Log.d("CalendarPage", "End Date: $endDate")

        val call =
            RetrofitClient.retrofitService.downloadPDF(startDate, endDate, "Bearer $token")
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        // Coroutine을 사용해 백그라운드 스레드에서 파일 저장 작업을 수행
                        CoroutineScope(Dispatchers.IO).launch {
                            saveFileToStorage(responseBody)
                        }
                    } ?: run {
                        Toast.makeText(
                            context,
                            "Failed to download PDF: Empty response body",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Failed to download PDF: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun getAuthToken(): String {
        val sharedPrefs = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPrefs?.getString("AuthToken", "") ?: ""
    }

    private suspend fun saveFileToStorage(body: ResponseBody) {
        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "downloaded_report.pdf"
            )

            // 파일이 이미 존재하는 경우 삭제합니다.
            if (file.exists()) {
                file.delete()
            }

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)
                outputStream.use { output ->
                    val buffer = ByteArray(4 * 1024) // buffer size
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }

                // 파일 저장 후 결과를 메인 스레드에서 UI로 알림
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "PDF 변환 성공", Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Failed to save the file: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Failed to save the file: ${e.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }



    private val diaryCountsMap = mutableMapOf<LocalDate, Int>()

    private fun fetchDiaryCounts(year: Int, month: Int) {
        val token = getTokenFromSession()

        if (token.isBlank()) {
            Toast.makeText(context, "Auth token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val firstDateOfMonth = LocalDate.of(year, month, 1)
        val dateString = firstDateOfMonth.format(DateTimeFormatter.ISO_LOCAL_DATE)

        RetrofitClient.retrofitService.getDiaryCount(dateString, token).enqueue(object :
            Callback<DiaryCountResponse> {
            override fun onResponse(
                call: Call<DiaryCountResponse>,
                response: Response<DiaryCountResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        diaryCountsMap.clear()
                        for (dayEntry in responseBody.day) {
                            val date = LocalDate.parse(dayEntry.date)
                            diaryCountsMap[date] = dayEntry.count
                        }
                        binding.calendarView.notifyCalendarChanged()
                    } else {
                        Toast.makeText(context, "Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(
                        "calendarDiaryCount",
                        "Failed with status code: ${response.code()}, message: ${response.message()}"
                    )
                    Toast.makeText(
                        context,
                        "Failed to load calendarDiaryCount data: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<DiaryCountResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun redirectToLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun onDateSelected(date: LocalDate) {
        if (isRangeSelectionEnabled) {
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
        val diaryCount = diaryCountsMap[LocalDate.parse(date)] ?: 0
        val bottomSheetFragment = BottomSheetFragment().apply {
            arguments = Bundle().apply {
                putString("date", date)
                putInt("diaryCount", diaryCount)
            }
        }
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun getTokenFromSession(): String {
        val sharedPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("AuthToken", "") ?: ""
        Log.d("GraphWeightBMIFragment", "Fetched token from session: $token")
        return "Bearer $token"
    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }

    private class MonthViewContainer(view: View) : ViewContainer(view) {
        val textViewSunday: TextView = view.findViewById(R.id.sunday)
        val textViewMonday: TextView = view.findViewById(R.id.monday)
        val textViewTuesday: TextView = view.findViewById(R.id.tuesday)
        val textViewWednesday: TextView = view.findViewById(R.id.wednesday)
        val textViewThursday: TextView = view.findViewById(R.id.thursday)
        val textViewFriday: TextView = view.findViewById(R.id.friday)
        val textViewSaturday: TextView = view.findViewById(R.id.saturday)
    }
}
