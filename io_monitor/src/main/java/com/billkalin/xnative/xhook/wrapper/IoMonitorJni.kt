@file:JvmName("IoMonitorJniKt")
package com.billkalin.xnative.xhook.wrapper


class IoMonitorJni {

    init {
        System.loadLibrary("io-monitor")
    }

    external fun doHook(): Boolean

    external fun doUnHook(): Boolean
}

fun getJavaContext(): JavaContext = JavaContext()

class JavaContext {
    private var stackString: String = ""
    private var threadName: String = ""

    init {
        threadName = Thread.currentThread().name
        stackString = IoMonitorUtils.stackTraceToString(Throwable())
    }
}