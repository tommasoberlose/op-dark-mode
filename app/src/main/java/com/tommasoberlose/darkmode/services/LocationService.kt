package com.tommasoberlose.darkmode.services

import android.Manifest
import android.app.Service
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.LocationServices
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.components.events.MainUiEvent
import com.tommasoberlose.darkmode.global.Constants
import com.tommasoberlose.darkmode.global.Preferences
import com.tommasoberlose.darkmode.helpers.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.greenrobot.eventbus.EventBus
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class LocationService : Service() {

    private val jobs: ArrayList<Job> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        startForeground(NotificationHelper.LOCATION_ACCESS_NOTIFICATION_ID, NotificationHelper.getLocationAccessNotification(this@LocationService))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            jobs += GlobalScope.launch(Dispatchers.IO) {
                EventBus.getDefault().post(MainUiEvent(isLoading = true))
                val location =
                    LocationServices.getFusedLocationProviderClient(this@LocationService).lastLocation.await()
                if (location != null) {
                    Preferences.latitude = "${location.latitude}"
                    Preferences.longitude = "${location.longitude}"

                    if (Preferences.automaticMode == Constants.AutomaticMode.SUNRISE_SUNSET_BASED) {
                        SunsetSunriseService.requestSunsetSunriseTime(this@LocationService)
                    }

                    updateAddress(location.latitude, location.longitude)
                }
                EventBus.getDefault().post(MainUiEvent(isLoading = false))
                stopSelf()
            }
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    private fun updateAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())

        jobs += GlobalScope.launch(Dispatchers.IO) {
            try {
                NotificationHelper.showRunningNotification(this@LocationService)
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

    override fun onDestroy() {
        super.onDestroy()
        jobs.forEach {
            it.cancel()
        }
    }

    companion object {

        @JvmStatic
        fun requestNewLocation(context: Context) {
            context.startForegroundService(Intent(context, LocationService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}