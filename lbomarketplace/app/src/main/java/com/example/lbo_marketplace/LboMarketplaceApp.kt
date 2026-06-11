package com.example.lbo_marketplace

import android.app.Application
import com.onesignal.OneSignal

// Base Application class of the Android app
class LboMarketplaceApp : Application() {

    // Lifecycle method called when the application is starting
    override fun onCreate() {
        // Invoke superclass implementation of onCreate
        super.onCreate()

        // Initialize the OneSignal SDK with the Application context
        OneSignal.initWithContext(this)

        // Set the OneSignal App ID loaded dynamically from BuildConfig (injected via local.properties)
        OneSignal.setAppId(
            BuildConfig.ONESIGNAL_APP_ID
        )
    }
}
