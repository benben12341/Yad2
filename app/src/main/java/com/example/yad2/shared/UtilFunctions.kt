package com.example.yad2.shared

import android.text.format.DateFormat
import java.util.Calendar
import java.util.Locale

object UtilFunctions {
    fun getDate(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time * 1000

        return DateFormat.format("dd-MM-yyyy", cal).toString()
    }
}
