package com.billkalin.justapp.crash

import android.os.Looper

class UninstallCrashCatcherException : Throwable()

object CrashCatcher {

    @Volatile
    private var installed: Boolean = false
    private var sDefaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    @JvmStatic
    fun install(handler: Thread.UncaughtExceptionHandler? = null) {
        if (installed)
            return
        installed = true
        sDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        android.os.Handler(Looper.getMainLooper()).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    if (e is UninstallCrashCatcherException) {
                        return@post
                    }
                    handler?.uncaughtException(Looper.getMainLooper().thread, e)
                }
            }
        }
        Thread.setDefaultUncaughtExceptionHandler(handler)
    }

    @JvmStatic
    fun unInstall() {
        if (!installed)
            return
        installed = false
        Thread.setDefaultUncaughtExceptionHandler(sDefaultUncaughtExceptionHandler)
        android.os.Handler(Looper.getMainLooper()).post {
            throw UninstallCrashCatcherException()
        }
    }
}