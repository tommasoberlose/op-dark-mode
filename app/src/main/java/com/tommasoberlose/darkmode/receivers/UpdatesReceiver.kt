package com.tommasoberlose.darkmode.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tommasoberlose.darkmode.global.Actions
import com.tommasoberlose.darkmode.helpers.DarkThemeHelper

class UpdatesReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ciao", "onReceive: ${intent.action}")
        when (intent.action) {
            Actions.ACTION_UPDATE_DARK_MODE_ON -> DarkThemeHelper.toggleDarkTheme(context, true)
            Actions.ACTION_UPDATE_DARK_MODE_OFF -> DarkThemeHelper.toggleDarkTheme(context, false)
        }
    }
}
