package com.billkalin.justapp

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat

class JustApp : Application() {

    companion object {
        lateinit var instance: JustApp
            private set
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}