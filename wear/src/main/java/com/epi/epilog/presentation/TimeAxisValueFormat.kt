package com.epi.epilog.presentation

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class TimeAxisValueFormat: IndexAxisValueFormatter() {

    override fun getFormattedValue(value: Float): String {


        //Float(min) -> Date
        var valueToMinutes = TimeUnit.MINUTES.toMillis(value.toLong())
        var timeMinutes = Date(valueToMinutes)
        var formatMinutes = SimpleDateFormat("HH: mm")

        return formatMinutes.format(timeMinutes)
    }
}