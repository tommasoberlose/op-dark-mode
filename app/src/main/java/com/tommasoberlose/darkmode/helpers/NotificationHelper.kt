package com.tommasoberlose.darkmode.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.ui.activities.MainActivity
import java.util.*

object NotificationHelper {
    private const val NOTIFICATION_ID = 14
    private const val RUNNING_NOTIFICATION_ID = 26
    const val LOCATION_ACCESS_NOTIFICATION_ID = 28

    fun showSecurePermissionNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            // Create channel
            createNotificationChannel(
                NotificationChannel(
                    context.getString(R.string.missing_permission_notification_channel_id),
                    context.getString(R.string.missing_permission_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.missing_permission_notification_channel_description)
                }
            )

            val builder = Notification.Builder(context, context.getString(R.string.missing_permission_notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification_dark_theme)
                .setContentTitle(context.getString(R.string.missing_secure_permission_notification_title))
                .setStyle(Notification.BigTextStyle().bigText(context.getString(R.string.missing_secure_permission_notification_subtitle)))
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))

            // Main intent that open the activity
            builder.setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

            notify(NOTIFICATION_ID, builder.build())
        }
    }

    fun hideSecurePermissionNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            cancel(NOTIFICATION_ID)
        }
    }

    fun showRunningNotification(context: Context): Notification {
        with(NotificationManagerCompat.from(context)) {
            // Create channel
            createNotificationChannel(
                NotificationChannel(
                    context.getString(R.string.running_notification_channel_id),
                    context.getString(R.string.running_notification_channel_name),
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = context.getString(R.string.running_notification_channel_description)
                }
            )

            val builder = Notification.Builder(context, context.getString(R.string.running_notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification_dark_theme)
                .setContentTitle(context.getString(R.string.running_notification_title))
                .setStyle(Notification.BigTextStyle().bigText(context.getString(R.string.running_notification_subtitle)))
                .setOngoing(true)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))

            // Main intent that open the activity
            builder.setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

            val notification = builder.build()
            notify(RUNNING_NOTIFICATION_ID, notification)

            return notification
        }
    }

    fun hideRunningNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            cancel(RUNNING_NOTIFICATION_ID)
        }
    }

    fun getLocationAccessNotification(context: Context): Notification {
        with(NotificationManagerCompat.from(context)) {
            // Create channel
            createNotificationChannel(
                NotificationChannel(
                    context.getString(R.string.running_notification_channel_id),
                    context.getString(R.string.running_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = context.getString(R.string.running_notification_channel_description)
                }
            )

            val builder = Notification.Builder(context, context.getString(R.string.running_notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification_dark_theme)
                .setContentTitle(context.getString(R.string.location_access_notification_title))
                .setStyle(Notification.BigTextStyle().bigText(context.getString(R.string.location_access_notification_subtitle)))
                .setOngoing(true)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))

            // Main intent that open the activity
            builder.setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

            return builder.build()
        }
    }
}