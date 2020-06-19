package com.tommasoberlose.darkmode.helpers

import android.app.UiModeManager
import android.content.Context
import android.provider.Settings


object DarkThemeHelper {

    private const val UI_NIGHT_MODE = "ui_night_mode"

    fun toggleDarkTheme(context: Context, enable: Boolean? = null) {

        with(context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager) {
            enableCarMode(0)

            Settings.Secure.putInt(context.contentResolver, UI_NIGHT_MODE, when (enable) {
                true -> UiModeManager.MODE_NIGHT_YES
                false -> UiModeManager.MODE_NIGHT_NO
                else -> {
                    when (nightMode) {
                        UiModeManager.MODE_NIGHT_NO -> UiModeManager.MODE_NIGHT_YES
                        else -> UiModeManager.MODE_NIGHT_NO
                    }
                }
            })

            disableCarMode(0)
        }
    }
}