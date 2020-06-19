package com.tommasoberlose.darkmode.helpers

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import com.tommasoberlose.darkmode.global.Preferences
import java.lang.Exception
import java.util.*

object TimeHelper {
    fun getHour(time: String): Int {
        return try {
            time.split(":").first().toInt()
        } catch (ignored: Exception) {
            0
        }
    }
    fun getMinute(time: String): Int {
        return try {
            time.split(":").last().toInt()
        } catch (ignored: Exception) {
            0
        }
    }
    fun getRangeCalendars(): Pair<Calendar, Calendar> {
        val now = Calendar.getInstance()

        val start = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, TimeHelper.getHour(Preferences.startTime))
            set(Calendar.MINUTE, TimeHelper.getMinute(Preferences.startTime))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, TimeHelper.getHour(Preferences.endTime))
            set(Calendar.MINUTE, TimeHelper.getMinute(Preferences.endTime))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return Pair(start, end)
    }
    fun getSunsetSunriseCalendars(): Pair<Calendar, Calendar> {
        val now = Calendar.getInstance()

        val sunset = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, TimeHelper.getHour(Preferences.sunsetTime))
            set(Calendar.MINUTE, TimeHelper.getMinute(Preferences.sunsetTime))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sunrise = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, TimeHelper.getHour(Preferences.sunriseTime))
            set(Calendar.MINUTE, TimeHelper.getMinute(Preferences.sunriseTime))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return Pair(sunset, sunrise)
    }
    fun getFormattedTime(context: Context, time: Calendar): String {
        return DateUtils.formatDateTime(context, time.timeInMillis, DateUtils.FORMAT_SHOW_TIME)
    }
}