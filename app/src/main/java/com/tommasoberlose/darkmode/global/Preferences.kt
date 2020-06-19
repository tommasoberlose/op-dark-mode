package com.tommasoberlose.darkmode.global

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref

object Preferences : KotprefModel() {
    override val commitAllPropertiesByDefault: Boolean = true

    var automaticMode by enumValuePref(Constants.AutomaticMode.DISABLED)
    var isTileAdded by booleanPref(default = false)

    var startTime by stringPref(default = "22:00")
    var endTime by stringPref(default = "07:00")

    var sunsetTime by stringPref(default = "22:00")
    var sunriseTime by stringPref(default = "07:00")
    var lastSunsetSunriseCheck by longPref(-1)

    var hideQuickSettingsTileTip by booleanPref(default = false)
    var hideOnePlusWarning by booleanPref(default = false)
}
