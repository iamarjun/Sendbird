package com.arjun.sendbird

import android.app.Application
import com.sendbird.android.SendBird

class SendbirdApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SendBird.init("28C8C093-BA94-49C2-B0DF-D53165C9DD57", this)
    }
}