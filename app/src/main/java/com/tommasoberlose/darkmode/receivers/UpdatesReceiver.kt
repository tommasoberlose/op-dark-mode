package com.tommasoberlose.darkmode.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tommasoberlose.darkmode.global.Actions
import com.tommasoberlose.darkmode.global.Preferences
import com.tommasoberlose.darkmode.helpers.DarkThemeHelper
import com.tommasoberlose.darkmode.helpers.NotificationHelper
import com.tommasoberlose.darkmode.services.SchedulerJob

class UpdatesReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Actions.ACTION_UPDATE_DARK_MODE_ON -> {
                if (!Preferences.toggleThemeRequiresDeviceIdle) {
                    DarkThemeHelper.toggleDarkTheme(context, true)
                } else {
                    SchedulerJob.scheduleService(context, true)
                }
            }
            Actions.ACTION_UPDATE_DARK_MODE_OFF -> {
                if (!Preferences.toggleThemeRequiresDeviceIdle) {
                    DarkThemeHelper.toggleDarkTheme(context, false)
                } else {
                    SchedulerJob.scheduleService(context, false)
                }
            }
        }
    }
}
