package com.billkalin.feature.device

import android.app.Application
import android.os.Build
import com.billkalin.justapp.bundle.DeviceFeature

class DeviceFeatureImpl : DeviceFeature {

    private lateinit var app: Application

    override fun initFeature(application: Application) {
        app = application
    }

    override fun getDeviceModel(): String {
        return Build.MODEL.toString()
    }

    override fun getPackageName(): String = app.packageName

}