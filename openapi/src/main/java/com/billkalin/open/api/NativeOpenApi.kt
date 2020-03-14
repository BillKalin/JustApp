package com.billkalin.open.api

import android.os.Build
//方法一：通过修改runtime 的hide api flag，绕过调用hide api的权限检查
object NativeOpenApi {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            System.loadLibrary("reflect-api")
        }
    }
    external fun openApi(targetSdkVersion: Int): Int
}