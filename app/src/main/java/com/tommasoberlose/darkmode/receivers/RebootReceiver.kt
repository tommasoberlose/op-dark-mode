package com.tommasoberlose.darkmode.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tommasoberlose.darkmode.services.SunsetSunriseService
import com.tommasoberlose.darkmode.services.UpdatesIntentService

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_DATE_CHANGED -> {
                UpdatesIntentService.setUpdates(context)
                SunsetSunriseService.requestSunsetSunriseTime(context)
            }
        }
    }
}
