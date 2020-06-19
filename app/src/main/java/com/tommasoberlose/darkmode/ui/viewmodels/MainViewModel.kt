package com.tommasoberlose.darkmode.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chibatching.kotpref.livedata.asLiveData
import com.tommasoberlose.darkmode.global.Preferences

class MainViewModel : ViewModel() {
    val isTileAdded = Preferences.asLiveData(Preferences::isTileAdded)
    val automaticMode = Preferences.asLiveData(Preferences::automaticMode)

    // Custom time range
    val startTime = Preferences.asLiveData(Preferences::startTime)
    val endTime = Preferences.asLiveData(Preferences::endTime)

    // Custom time range
    val sunriseTime = Preferences.asLiveData(Preferences::sunriseTime)
    val sunsetTime = Preferences.asLiveData(Preferences::sunsetTime)
}