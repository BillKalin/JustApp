package com.billkalin.justapp

import android.app.Application
import android.content.Context
import com.billkalin.hook.HookUtils
import com.billkalin.justapp.crash.CrashPrinter
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
        HookUtils.hookInstrumentation()
//        println(CrashPrinter::class.java.simpleName)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}