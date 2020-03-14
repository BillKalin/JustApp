package com.billkalin.open.api

import android.os.Build

object OpenApi {

    fun open(useNative: Boolean = true, targetVersion: Int = Build.VERSION_CODES.P): Boolean {
        if (useNative) {
            return NativeOpenApi.openApi(targetVersion) == 0
        }
        return JavaOpenApi.openAllApi()
    }

}