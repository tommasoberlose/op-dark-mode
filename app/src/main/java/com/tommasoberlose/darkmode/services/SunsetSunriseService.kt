package com.tommasoberlose.darkmode.services

import android.Manifest
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.JobIntentService
import com.google.android.gms.location.LocationServices
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.components.events.MainUiEvent
import com.tommasoberlose.darkmode.global.Preferences
import com.tommasoberlose.darkmode.helpers.TimeHelper
import com.tommasoberlose.darkmode.models.Results
import com.tommasoberlose.darkmode.network.SunsetSunriseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

class SunsetSunriseService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        if (Preferences.latitude != "0" && Preferences.longitude != "0") {
            EventBus.getDefault().post(MainUiEvent(isLoading = true))
            GlobalScope.launch(Dispatchers.IO) {
                val response = SunsetSunriseRepository.getSunsetSunriseTime(
                    Preferences.latitude.toDouble(),
                    Preferences.longitude.toDouble()
                )
                if (response != null) {
                    val results: Results? = response.results
                    if (results != null) {
                        val sunset = ZonedDateTime.parse(results.sunset)
                            .withZoneSameInstant(ZoneId.systemDefault())
                        val sunrise = ZonedDateTime.parse(results.sunrise)
                            .withZoneSameInstant(ZoneId.systemDefault())
                        Preferences.sunsetTime = TimeHelper.getStandardDate(GregorianCalendar.from(sunset)).apply { Log.d("ciao", this) }
                        Preferences.sunriseTime = TimeHelper.getStandardDate(GregorianCalendar.from(sunrise)).apply { Log.d("ciao", this) }
                        Preferences.lastSunsetSunriseCheck =
                            Calendar.getInstance().timeInMillis
                        UpdatesIntentService.setUpdates(this@SunsetSunriseService)

                        EventBus.getDefault()
                            .post(MainUiEvent(isLoading = false))
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
        } else {
            LocationService.requestNewLocation(this)
        }
    }

    companion object {

        private const val JOB_ID = 1002

        @JvmStatic
        fun requestSunsetSunriseTime(context: Context) {
            with (context.applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler) {
                cancel(JOB_ID)
                enqueueWork(context, SunsetSunriseService::class.java, JOB_ID, Intent(context, SunsetSunriseService::class.java))
            }
        }
    }
}