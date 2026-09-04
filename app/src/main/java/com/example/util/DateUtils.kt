package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val shortMonthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())

    fun getTodayDateString(): String {
        return isoFormat.format(Date())
    }

    fun formatDateForDisplay(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: Date()
            displayDateFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getShortMonthYear(date: Date = Date()): String {
        return shortMonthFormat.format(date)
    }

    fun getDayOfWeekShort(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: Date()
            dayOfWeekFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun getDateOffsetDays(offset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offset)
        return isoFormat.format(cal.time)
    }

    fun getPastDaysList(count: Int): List<String> {
        val list = mutableListOf<String>()
        for (i in (count - 1) downTo 0) {
            list.add(getDateOffsetDays(-i))
        }
        return list
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFirstDayOfWeekInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, etc.
    }
}
