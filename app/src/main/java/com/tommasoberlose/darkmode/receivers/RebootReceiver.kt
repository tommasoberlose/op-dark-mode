package com.tommasoberlose.darkmode.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tommasoberlose.darkmode.global.Constants
import com.tommasoberlose.darkmode.global.Preferences
import com.tommasoberlose.darkmode.services.LocationService
import com.tommasoberlose.darkmode.services.SunsetSunriseService
import com.tommasoberlose.darkmode.services.UpdatesIntentService

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                UpdatesIntentService.setUpdates(context)
                if (Preferences.automaticMode == Constants.AutomaticMode.SUNRISE_SUNSET_BASED) {
                    SunsetSunriseService.requestSunsetSunriseTime(context)
                }
            }
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED -> {
                UpdatesIntentService.setUpdates(context)
                LocationService.requestNewLocation(context)
            }
            Intent.ACTION_DATE_CHANGED -> {
                SunsetSunriseService.requestSunsetSunriseTime(context)
            }
        }
    }
}
