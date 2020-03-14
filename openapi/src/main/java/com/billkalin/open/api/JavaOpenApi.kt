package com.billkalin.open.api

import android.os.Build
import android.util.Log
import java.lang.reflect.Method

object JavaOpenApi {

    private var vmRuntime: Any? = null
    private var setHiddenApiExemptions: Method? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val forName = Class::class.java.getMethod("forName", String::class.java)
                val getDeclaredMethod = Class::class.java.getDeclaredMethod(
                    "getDeclaredMethod",
                    String::class.java,
                    arrayOf(Any::class.java)::class.java
                )
                val runtimeCls = forName.invoke(null, "dalvik.system.VMRuntime")
                val runtimeMethod =
                    getDeclaredMethod.invoke(runtimeCls, "getRuntime", null) as Method
                setHiddenApiExemptions = getDeclaredMethod.invoke(
                    runtimeCls, "setHiddenApiExemptions",
                    arrayOf<Class<*>>(Array<String>::class.java)
                ) as Method
                vmRuntime = runtimeMethod.invoke(null)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(NativeOpenApi::class.java.simpleName, "$e")
                }
            }
        }
    }

    fun openApis(methods: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
            return true
        if (vmRuntime == null || setHiddenApiExemptions == null)
            return false
        try {
            setHiddenApiExemptions?.invoke(vmRuntime, methods)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(NativeOpenApi::class.java.simpleName, "openMethods ：$e")
            }
            return false
        }
        return true
    }

    fun openApi(method: String): Boolean {
        return openApis(arrayOf(method))
    }

    fun openAllApi(): Boolean {
        return openApis(arrayOf("L"))
    }
}