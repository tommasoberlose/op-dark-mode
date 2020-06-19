package com.tommasoberlose.darkmode.components

import android.app.Application
import com.chibatching.kotpref.Kotpref
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tommasoberlose.darkmode.BuildConfig

class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Firebase crashlitycs
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        // Preferences
        Kotpref.init(this)
    }
}