package com.tommasoberlose.darkmode.services

import android.Manifest
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import androidx.core.app.JobIntentService
import com.google.android.gms.location.LocationServices
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.components.events.MainUiEvent
import com.tommasoberlose.darkmode.global.Constants
import com.tommasoberlose.darkmode.global.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.greenrobot.eventbus.EventBus
import java.lang.Exception
import java.util.*


class LocationService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            EventBus.getDefault().post(MainUiEvent(isLoading = true))
            GlobalScope.launch(Dispatchers.IO) {
                val location = LocationServices.getFusedLocationProviderClient(this@LocationService).lastLocation.await()
                if (location != null) {
                    Preferences.latitude = "${location.latitude}"
                    Preferences.longitude = "${location.longitude}"

                    if (Preferences.automaticMode == Constants.AutomaticMode.SUNRISE_SUNSET_BASED) {
                        SunsetSunriseService.requestSunsetSunriseTime(this@LocationService)
                    }

                    updateAddress(location.latitude, location.longitude)
                    EventBus.getDefault().post(MainUiEvent(isLoading = false))
                }
            }
        }
    }

    private fun updateAddress(latitude: Double, longitude: Double) {
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())

        GlobalScope.launch(Dispatchers.IO) {
            try {
            val addresses: List<Address> = geocoder.getFromLocation(
                latitude,
                longitude,
                1
            )
            val address: String =
                addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

            val city: String = addresses[0].locality
            val state: String = addresses[0].adminArea
            val country: String = addresses[0].countryName
            val postalCode: String = addresses[0].postalCode

            Preferences.location = "$city, $country"
            } catch (ignored: Exception) {
                Preferences.location = "${getString(R.string.unknown_location)} (${latitude},${longitude})"
            }
        }
    }

    companion object {

        private const val JOB_ID = 1005

        @JvmStatic
        fun requestNewLocation(context: Context) {
            with (context.applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler) {
                cancel(JOB_ID)
                enqueueWork(context, LocationService::class.java, JOB_ID, Intent(context, LocationService::class.java))
            }
        }
    }
}