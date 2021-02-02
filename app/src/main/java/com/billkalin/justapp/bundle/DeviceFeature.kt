package com.billkalin.justapp.bundle

import android.app.Application

interface DeviceFeature {

    fun initFeature(application: Application)

    fun getDeviceModel(): String

    fun getPackageName(): String
}