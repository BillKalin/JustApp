package com.billkalin.justapp

import android.app.Application
import android.content.Context
import com.billkalin.justapp.fix.QZoneHotfix
import com.google.android.play.core.splitcompat.SplitCompat

class JustApp : Application() {

    companion object {
        lateinit var instance: JustApp
            private set
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
        QZoneHotfix.fix(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}