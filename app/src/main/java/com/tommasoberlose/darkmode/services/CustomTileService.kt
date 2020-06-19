package com.tommasoberlose.darkmode.services

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.global.Constants
import com.tommasoberlose.darkmode.global.Preferences
import com.tommasoberlose.darkmode.helpers.DarkThemeHelper
import com.tommasoberlose.darkmode.helpers.TimeHelper
import com.tommasoberlose.darkmode.utils.isDarkTheme
import java.util.*

class CustomTileService: TileService(){

    override fun onClick() {
        super.onClick()

        val tile = qsTile
        if (isDarkTheme()) {
            tile.state = Tile.STATE_INACTIVE
        } else {
            tile.state = Tile.STATE_ACTIVE
        }
        DarkThemeHelper.toggleDarkTheme(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = getSubtitle(!isDarkTheme())
        }
        tile.updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()

        Preferences.isTileAdded = true
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        Preferences.isTileAdded = false
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile
        tile.state = if (isDarkTheme()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = getSubtitle()
        }
        tile.updateTile()
    }

    private fun getSubtitle(isDark: Boolean = isDarkTheme()): String {
        val now = Calendar.getInstance()
        return when (Preferences.automaticMode) {
            Constants.AutomaticMode.TIME_BASED -> {
                val (start, end) = TimeHelper.getRangeCalendars()
                if (start.timeInMillis >= end.timeInMillis) {
                    if (now.timeInMillis > end.timeInMillis && now.timeInMillis < start.timeInMillis) {
                        if (!isDark) {
                            getString(R.string.start_at, TimeHelper.getFormattedTime(this, start))
                        } else {
                            getString(R.string.end_at, TimeHelper.getFormattedTime(this, start))
                        }
                    } else {
                        if (isDark) {
                            getString(R.string.end_at, TimeHelper.getFormattedTime(this, start))
                        } else {
                            getString(R.string.start_at, TimeHelper.getFormattedTime(this, start))
                        }
                    }
                } else {
                    if (now.timeInMillis > start.timeInMillis && now.timeInMillis < end.timeInMillis) {
                        if (isDark) {
                            getString(R.string.end_at, TimeHelper.getFormattedTime(this, start))
                        } else {
                            getString(R.string.start_at, TimeHelper.getFormattedTime(this, start))
                        }
                    } else {
                        if (!isDark) {
                            getString(R.string.start_at, TimeHelper.getFormattedTime(this, start))
                        } else {
                            getString(R.string.end_at, TimeHelper.getFormattedTime(this, start))
                        }
                    }
                }
            }
            Constants.AutomaticMode.SUNRISE_SUNSET_BASED -> {
                val (start, end) = TimeHelper.getSunsetSunriseCalendars()
                if (start.timeInMillis >= end.timeInMillis) {
                    if (now.timeInMillis > end.timeInMillis && now.timeInMillis < start.timeInMillis) {
                        if (!isDark) {
                            getString(R.string.start_at_sunset)
                        } else {
                            getString(R.string.end_at_sunrise)
                        }
                    } else {
                        if (isDark) {
                            getString(R.string.end_at_sunrise)
                        } else {
                            getString(R.string.start_at_sunset)
                        }
                    }
                } else {
                    if (now.timeInMillis > start.timeInMillis && now.timeInMillis < end.timeInMillis) {
                        if (isDark) {
                            getString(R.string.end_at_sunrise)
                        } else {
                            getString(R.string.start_at_sunset)
                        }
                    } else {
                        if (!isDark) {
                            getString(R.string.start_at_sunset)
                        } else {
                            getString(R.string.end_at_sunrise)
                        }
                    }
                }
            }
            else -> {
                ""
            }
        }
    }
}