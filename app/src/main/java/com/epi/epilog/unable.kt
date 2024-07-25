package com.epi.epilog

import androidx.fragment.app.Fragment

class unable : Fragment() {

//    private var _binding: FragmentCalendarBinding? = null
//    private val binding get() = _binding!!
//
//    lateinit var calendarAdapter: CalendarAdapter
//    private var calendarList = ArrayList<CalendarVO>()
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private var currentWeekStartDate: LocalDateTime = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(
//        DayOfWeek.SUNDAY))
//
//    companion object {
//        fun newInstance() = unable()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
//        return _binding?.root
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // 여기에서 calendarAdapter를 초기화하는 것이 좋습니다.
//        calendarAdapter = CalendarAdapter(calendarList)
//        binding.weekRecycler.adapter = calendarAdapter
//        binding.weekRecycler.layoutManager = GridLayoutManager(context, 7)
//
//        initWeekDates()
//
//        binding.prevImageView.setOnClickListener {
//            currentWeekStartDate = currentWeekStartDate.minusWeeks(1)
//            initWeekDates()
//        }
//
//        binding.nextImageView.setOnClickListener {
//            currentWeekStartDate = currentWeekStartDate.plusWeeks(1)
//            initWeekDates()
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun initWeekDates() {
//        val week_day: Array<String> = resources.getStringArray(R.array.calendar_day)
//        calendarList.clear()
//
//        val dateFormat = DateTimeFormatter.ofPattern("dd").withLocale(Locale.forLanguageTag("ko"))
//        val monthFormat = DateTimeFormatter.ofPattern("yyyy년 MM월").withLocale(Locale.forLanguageTag("ko"))
//        val localDate = currentWeekStartDate.format(monthFormat)
//        binding.textYearMonth.text = localDate
//
//        for (i in 0 until 7) {
//            val date = currentWeekStartDate.plusDays(i.toLong())
//            calendarList.add(CalendarVO(date.format(dateFormat), week_day[date.dayOfWeek.value % 7]))
//        }
//
//        calendarAdapter.notifyDataSetChanged()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}

