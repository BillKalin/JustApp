package com.billkalin.justapp

import android.app.Application

class JustApp : Application() {

    companion object {
        lateinit var instance: JustApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}