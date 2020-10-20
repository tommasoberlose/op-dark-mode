package com.tommasoberlose.darkmode.services

import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import com.tommasoberlose.darkmode.global.Actions
import com.tommasoberlose.darkmode.helpers.DarkThemeHelper


class SchedulerJob : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        DarkThemeHelper.toggleDarkTheme(this, params.extras.getBoolean(EXTRA_ENABLED))
        return true
    }
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    companion object {
        private const val JOB_ID = 1026
        private const val EXTRA_ENABLED = "EXTRA_ENABLED"

        fun scheduleService(context: Context, enable: Boolean) {
            val serviceName = ComponentName(context, SchedulerJob::class.java)
            val jobInfo = JobInfo.Builder(
                JOB_ID,
                serviceName
            )
                .setRequiresDeviceIdle(true)
                .setPersisted(true)
                .setOverrideDeadline(1000 * 60 * 5)
                .setExtras(PersistableBundle().apply { putBoolean(EXTRA_ENABLED, enable) })
                .build()
            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            scheduler.cancel(JOB_ID)
            scheduler.schedule(jobInfo)
        }
    }
}