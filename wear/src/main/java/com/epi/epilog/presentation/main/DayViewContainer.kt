package com.epi.epilog.presentation.main

import android.view.View
import android.widget.TextView
import com.epi.epilog.R
import com.kizitonwose.calendar.view.ViewContainer

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarDayText)
}