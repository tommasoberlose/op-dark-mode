package com.tommasoberlose.darkmode.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobScheduler
import android.content.Intent
import android.content.Context
import android.util.Log
import androidx.core.app.JobIntentService
import com.tommasoberlose.darkmode.global.Actions
import com.tommasoberlose.darkmode.global.Constants
import com.tommasoberlose.darkmode.global.Preferences
import com.tommasoberlose.darkmode.helpers.TimeHelper
import com.tommasoberlose.darkmode.receivers.UpdatesReceiver
import java.util.*

class UpdatesIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        with(getSystemService(Context.ALARM_SERVICE) as AlarmManager) {
            // Remove alarms
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, SUNSET_SUNRISE_ALARM_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, SUNSET_ALARM_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, SUNRISE_ALARM_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, START_ALARM_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            cancel(PendingIntent.getBroadcast(this@UpdatesIntentService, END_ALARM_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

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
                        PendingIntent.getBroadcast(this@UpdatesIntentService, START_ALARM_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java).apply { action = Actions.ACTION_UPDATE_DARK_MODE_ON }, PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                    setRepeating(
                        AlarmManager.RTC,
                        end.apply {
                            if (now.timeInMillis > timeInMillis) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }.timeInMillis,
                        1000 * 60 * 60 * 24,
                        PendingIntent.getBroadcast(this@UpdatesIntentService, END_ALARM_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java).apply { action = Actions.ACTION_UPDATE_DARK_MODE_OFF }, PendingIntent.FLAG_UPDATE_CURRENT)
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
                        PendingIntent.getBroadcast(this@UpdatesIntentService, SUNSET_ALARM_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java).apply { action = Actions.ACTION_UPDATE_DARK_MODE_ON }, PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                    setRepeating(
                        AlarmManager.RTC,
                        sunrise.apply {
                            if (now.timeInMillis > timeInMillis) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }.timeInMillis,
                        1000 * 60 * 60 * 24,
                        PendingIntent.getBroadcast(this@UpdatesIntentService, SUNRISE_ALARM_ID, Intent(this@UpdatesIntentService, UpdatesReceiver::class.java).apply { action = Actions.ACTION_UPDATE_DARK_MODE_OFF }, PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                }

                else -> {}
            }
        }
    }

    companion object {
        private const val SUNSET_SUNRISE_ALARM_ID = 54
        private const val START_ALARM_ID = 56
        private const val END_ALARM_ID = 58
        private const val SUNSET_ALARM_ID = 60
        private const val SUNRISE_ALARM_ID = 62

        private const val JOB_ID = 1001

        @JvmStatic
        fun setUpdates(context: Context) {
            with (context.applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler) {
                cancel(JOB_ID)
                enqueueWork(
                    context,
                    UpdatesIntentService::class.java,
                    JOB_ID,
                    Intent(context, UpdatesIntentService::class.java)
                )
            }
        }
    }
}
