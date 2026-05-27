package com.example.lbo_marketplace

import android.app.Application
import com.onesignal.OneSignal

class LboMarketplaceApp : Application() {

    override fun onCreate() {

        super.onCreate()

        // 🔥 Initialize OneSignal

        OneSignal.initWithContext(this)

        // 🔥 Set OneSignal App ID

        OneSignal.setAppId(
            "ea563567-0403-4d31-af1a-235130fbbd6b"
        )
    }
}
