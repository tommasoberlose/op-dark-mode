package com.tommasoberlose.darkmode.services

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.util.Log
import com.tommasoberlose.darkmode.global.Actions
import com.tommasoberlose.darkmode.global.Constants
import com.tommasoberlose.darkmode.global.Preferences
import com.tommasoberlose.darkmode.helpers.TimeHelper
import com.tommasoberlose.darkmode.receivers.UpdatesReceiver
import java.util.*

class UpdatesIntentService : IntentService("UpdatesIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        with(getSystemService(Context.ALARM_SERVICE) as AlarmManager) {

            // Remove alarms
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, SUNSET_SUNRISE_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, SUNSET_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, SUNRISE_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, START_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, END_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

            val now = Calendar.getInstance()

            when (Preferences.automaticMode) {
                Constants.AutomaticMode.TIME_BASED -> {
                    val (start, end) = TimeHelper.getRangeCalendars()
                    setRepeating(
                        AlarmManager.RTC,
                        start.apply {
                            if (now.timeInMillis > timeInMillis) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }.timeInMillis,
                        1000 * 60 * 60 * 24,
                        PendingIntent.getBroadcast(this@UpdatesIntentService, START_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java).apply { action = Actions.ACTION_UPDATE_DARK_MODE_ON }, PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                    setRepeating(
                        AlarmManager.RTC,
                        end.apply {
                            if (now.timeInMillis > timeInMillis) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }.timeInMillis,
                        1000 * 60 * 60 * 24,
                        PendingIntent.getBroadcast(this@UpdatesIntentService, END_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java).apply { action = Actions.ACTION_UPDATE_DARK_MODE_OFF }, PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                }

                Constants.AutomaticMode.SUNRISE_SUNSET_BASED -> {
                    val (sunset, sunrise) = TimeHelper.getSunsetSunriseCalendars()
                    setRepeating(
                        AlarmManager.RTC,
                        sunset.apply {
                            if (now.timeInMillis > timeInMillis) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }.timeInMillis,
                        1000 * 60 * 60 * 24,
                        PendingIntent.getBroadcast(this@UpdatesIntentService, SUNSET_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java).apply { action = Actions.ACTION_UPDATE_DARK_MODE_ON }, PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                    setRepeating(
                        AlarmManager.RTC,
                        sunrise.apply {
                            if (now.timeInMillis > timeInMillis) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }.timeInMillis,
                        1000 * 60 * 60 * 24,
                        PendingIntent.getBroadcast(this@UpdatesIntentService, SUNRISE_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java).apply { action = Actions.ACTION_UPDATE_DARK_MODE_OFF }, PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                    setRepeating(
                        AlarmManager.RTC,
                        now.apply {
                            set(Calendar.MILLISECOND, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MINUTE, 5)
                            set(Calendar.HOUR_OF_DAY, 0)
                            add(Calendar.DAY_OF_YEAR, 1)
                        }.timeInMillis,
                        1000 * 60 * 60 * 24,
                        PendingIntent.getBroadcast(this@UpdatesIntentService, SUNSET_SUNRISE_JOB_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java).apply { action = Actions.ACTION_UPDATE_DARK_MODE_OFF }, PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                }

                else -> {}
            }
        }
    }

    companion object {
        const val SUNSET_SUNRISE_JOB_ID = 54
        const val START_JOB_ID = 56
        const val END_JOB_ID = 58
        const val SUNSET_JOB_ID = 60
        const val SUNRISE_JOB_ID = 62

        @JvmStatic
        fun setUpdates(context: Context) {
            context.startService(Intent(context, UpdatesIntentService::class.java))
        }
    }
}
