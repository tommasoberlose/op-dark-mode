package com.tommasoberlose.darkmode.services

import android.Manifest
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.JobIntentService
import com.google.android.gms.location.LocationServices
import com.tommasoberlose.darkmode.global.Constants
import com.tommasoberlose.darkmode.global.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
            GlobalScope.launch(Dispatchers.IO) {
                val location = LocationServices.getFusedLocationProviderClient(this@LocationService).lastLocation.await()
                if (location != null) {
                    Preferences.latitude = "${location.latitude}"
                    Preferences.longitude = "${location.longitude}"

                    if (Preferences.automaticMode == Constants.AutomaticMode.SUNRISE_SUNSET_BASED) {
                        SunsetSunriseService.requestSunsetSunriseTime(this@LocationService)
                    }
                }
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