@file:JvmName("IoMonitorJniKt")
package com.billkalin.xnative.xhook.wrapper

import androidx.annotation.Keep

class IoMonitorJni {

    init {
        System.loadLibrary("io-monitor")
    }

    external fun doHook(): Boolean

    external fun doUnHook(): Boolean
}

@Keep
fun getJavaContext(): JavaContext = JavaContext()

@Keep
class JavaContext {
    private var stackString: String = ""
    private var threadName: String = ""

    init {
        threadName = Thread.currentThread().name
        stackString = IoMonitorUtils.stackTraceToString(Throwable())
    }
}