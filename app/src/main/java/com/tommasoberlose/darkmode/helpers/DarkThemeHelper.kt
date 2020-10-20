package com.tommasoberlose.darkmode.helpers

import android.app.UiModeManager
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.tommasoberlose.darkmode.components.events.MainUiEvent
import org.greenrobot.eventbus.EventBus


object DarkThemeHelper {

    private const val UI_NIGHT_MODE = "ui_night_mode"
    const val SECURE_PERMISSION_ERROR = "SECURE_PERMISSION_ERROR"

    fun toggleDarkTheme(context: Context, enable: Boolean? = null) {
        with(context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager) {
            try {
                Settings.Secure.putInt(
                    context.contentResolver, UI_NIGHT_MODE, when (enable) {
                        true -> UiModeManager.MODE_NIGHT_YES
                        false -> UiModeManager.MODE_NIGHT_NO
                        else -> {
                            when (nightMode) {
                                UiModeManager.MODE_NIGHT_NO -> UiModeManager.MODE_NIGHT_YES
                                else -> UiModeManager.MODE_NIGHT_NO
                            }
                        }
                    }
                )
                EventBus.getDefault().post(MainUiEvent())
                NotificationHelper.hideSecurePermissionNotification(context)
                enableCarMode(0)
            } catch (ignored: Exception) {
                EventBus.getDefault().post(MainUiEvent(error = SECURE_PERMISSION_ERROR))
                NotificationHelper.showSecurePermissionNotification(context)
            } finally {
                disableCarMode(0)
            }
        }
    }

    fun checkSecurePermission(context: Context, showNotification: Boolean = false): Boolean {
        with(context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager) {
            return try {
                // Try to write the setting without changing it
                Settings.Secure.putInt(context.contentResolver, UI_NIGHT_MODE, nightMode)
                EventBus.getDefault().post(MainUiEvent())
                NotificationHelper.hideSecurePermissionNotification(context)
                true
            } catch (ignored: Exception) {
                EventBus.getDefault().post(MainUiEvent(error = SECURE_PERMISSION_ERROR))
                if (showNotification) {
                    NotificationHelper.showSecurePermissionNotification(context)
                }
                false
            }
        }
    }
}