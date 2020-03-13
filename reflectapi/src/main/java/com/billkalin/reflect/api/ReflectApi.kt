package com.billkalin.reflect.api

import android.os.Build
import android.util.Log
import java.lang.reflect.Method

object ReflectApi {

    private var vmRuntime: Any? = null
    private var setHiddenApiExemptions: Method? = null

    init {
        System.loadLibrary("reflect-api")
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
                    Log.e(ReflectApi::class.java.simpleName, "$e")
                }
            }
        }
    }

    //方法一：通过修改runtime 的hide api flag，绕过调用hide api的权限检查
    //
    external fun openApi(targetSdkVersion: Int): Int

    fun openMethods(methods: Array<String>): Boolean {
        if (vmRuntime == null || setHiddenApiExemptions == null)
            return false
        try {
            setHiddenApiExemptions?.invoke(vmRuntime, methods)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(ReflectApi::class.java.simpleName, "openMethods ：$e")
            }
            return false
        }
        return true
    }

    fun openMethod(method: String): Boolean {
        return openMethods(arrayOf(method))
    }

    fun openAllMethod(): Boolean {
        return openMethods(arrayOf("L"))
    }
}