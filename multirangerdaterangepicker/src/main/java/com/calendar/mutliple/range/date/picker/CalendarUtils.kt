package com.calendar.mutliple.range.date.picker

import java.util.*
import java.util.Calendar.*

internal fun Calendar.toPrettyMonthString(
    style: Int = LONG,
    locale: Locale = Locale.getDefault()
): String {
    val month = getDisplayName(MONTH, style, locale)
    val year = get(YEAR).toString()
    return if (month == null) throw IllegalStateException("Cannot get pretty name")
    else "$month, $year"
}

internal fun Calendar.toPrettyDateString(locale: Locale = Locale.getDefault()): String {
    val day = get(DAY_OF_MONTH).toString()
    return "$day ${this.toPrettyMonthString(Calendar.SHORT, locale)}"
}

internal fun Calendar.isBefore(otherCalendar: Calendar): Boolean {
    return get(YEAR) == otherCalendar.get(YEAR)
            && get(MONTH) == otherCalendar.get(MONTH)
            && get(DAY_OF_MONTH) < otherCalendar.get(DAY_OF_MONTH)
}

internal fun Calendar.isAfter(otherCalendar: Calendar): Boolean {
    return get(YEAR) == otherCalendar.get(YEAR)
            && get(MONTH) == otherCalendar.get(MONTH)
            && get(DAY_OF_MONTH) > otherCalendar.get(DAY_OF_MONTH)
}

internal fun Calendar.totalMonthDifference(startCalendar: Calendar): Int {
    val yearDiff = get(YEAR) - startCalendar.get(YEAR)
    val monthDiff = get(MONTH) - startCalendar.get(MONTH)

    return monthDiff + (yearDiff * 12)
}