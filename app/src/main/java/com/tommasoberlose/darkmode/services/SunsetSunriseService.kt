package com.tommasoberlose.darkmode.services

import android.Manifest
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.components.events.MainUiEvent
import com.tommasoberlose.darkmode.global.Preferences
import com.tommasoberlose.darkmode.helpers.TimeHelper
import com.tommasoberlose.darkmode.models.Results
import com.tommasoberlose.darkmode.network.SunsetSunriseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.HashMap

class SunsetSunriseService : IntentService("SunsetSunriseService") {
    override fun onHandleIntent(intent: Intent?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val now = Calendar.getInstance()
            if (Preferences.lastSunsetSunriseCheck < 0 || Calendar.getInstance().apply { timeInMillis = Preferences.lastSunsetSunriseCheck }.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR)) {
                EventBus.getDefault().post(MainUiEvent(isLoading = true))
                LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val location = task.result
                        if (location != null) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val response = SunsetSunriseRepository.getSunsetSunriseTime(
                                    location.latitude,
                                    location.longitude
                                )
                                if (response != null) {
                                    val results: Results? = response.results
                                    if (results != null) {
                                        val sunset = ZonedDateTime.parse(results.sunset).withZoneSameInstant(ZoneId.systemDefault())
                                        val sunrise = ZonedDateTime.parse(results.sunrise).withZoneSameInstant(ZoneId.systemDefault())
                                        Preferences.sunsetTime = TimeHelper.getFormattedTime(this@SunsetSunriseService, GregorianCalendar.from(sunset))
                                        Preferences.sunriseTime = TimeHelper.getFormattedTime(this@SunsetSunriseService, GregorianCalendar.from(sunrise))
                                        Preferences.lastSunsetSunriseCheck = Calendar.getInstance().timeInMillis
                                        UpdatesIntentService.setUpdates(this@SunsetSunriseService)
                                        EventBus.getDefault().post(MainUiEvent(isLoading = false))
                                    } else {
                                        EventBus.getDefault().post(
                                            MainUiEvent(
                                                isLoading = false,
                                                error = getString(R.string.error_loading_sunset_and_sunrise)
                                            )
                                        )
                                    }
                                } else {
                                    EventBus.getDefault().post(
                                        MainUiEvent(
                                            isLoading = false,
                                            error = getString(R.string.error_loading_sunset_and_sunrise)
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        EventBus.getDefault().post(
                            MainUiEvent(
                                isLoading = false,
                                error = getString(R.string.error_loading_sunset_and_sunrise)
                            )
                        )
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun requestSunsetSunriseTime(context: Context) {
            context.startService(Intent(context, SunsetSunriseService::class.java))
        }
    }
}