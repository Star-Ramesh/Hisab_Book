package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
  private val dateFormatIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  private val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
  private val timeFormat12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
  private val displayDateHeaderFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
  private val shortDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

  fun getCurrentDateIso(): String = dateFormatIso.format(Date())

  fun getCurrentTime24(): String = timeFormat24.format(Date())

  fun formatDisplayTime(time24: String): String {
    return try {
      val parsed = timeFormat24.parse(time24)
      if (parsed != null) timeFormat12.format(parsed) else time24
    } catch (e: Exception) {
      time24
    }
  }

  fun formatDisplayDate(dateIso: String): String {
    return try {
      val today = getCurrentDateIso()
      val yesterday = getYesterdayDateIso()
      when (dateIso) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> {
          val parsed = dateFormatIso.parse(dateIso)
          if (parsed != null) displayDateHeaderFormat.format(parsed) else dateIso
        }
      }
    } catch (e: Exception) {
      dateIso
    }
  }

  fun formatDateHeaderUppercase(dateIso: String): String {
    return try {
      val today = getCurrentDateIso()
      val yesterday = getYesterdayDateIso()
      when (dateIso) {
        today -> "TODAY"
        yesterday -> "YESTERDAY"
        else -> {
          val parsed = dateFormatIso.parse(dateIso)
          if (parsed != null) displayDateHeaderFormat.format(parsed).uppercase(Locale.getDefault()) else dateIso
        }
      }
    } catch (e: Exception) {
      dateIso.uppercase(Locale.getDefault())
    }
  }

  fun formatShortDate(dateIso: String): String {
    return try {
      val parsed = dateFormatIso.parse(dateIso)
      if (parsed != null) shortDateFormat.format(parsed) else dateIso
    } catch (e: Exception) {
      dateIso
    }
  }

  fun getYesterdayDateIso(): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -1)
    return dateFormatIso.format(cal.time)
  }

  fun getStartOfWeekIso(): String {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    return dateFormatIso.format(cal.time)
  }

  fun getStartOfMonthIso(): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    return dateFormatIso.format(cal.time)
  }

  fun getStartOfLastMonthIso(): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, -1)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    return dateFormatIso.format(cal.time)
  }

  fun getEndOfLastMonthIso(): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.add(Calendar.DAY_OF_YEAR, -1)
    return dateFormatIso.format(cal.time)
  }

  fun computeEpochMillis(dateIso: String, time24: String): Long {
    return try {
      val combined = "$dateIso $time24"
      val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
      format.parse(combined)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
      System.currentTimeMillis()
    }
  }

  fun getDayOfWeekShort(dateIso: String): String {
    return try {
      val parsed = dateFormatIso.parse(dateIso) ?: return ""
      val cal = Calendar.getInstance().apply { time = parsed }
      when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Mon"
        Calendar.TUESDAY -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY -> "Thu"
        Calendar.FRIDAY -> "Fri"
        Calendar.SATURDAY -> "Sat"
        Calendar.SUNDAY -> "Sun"
        else -> ""
      }
    } catch (e: Exception) {
      ""
    }
  }
}
