package com.billkalin.breakpad

object NativeCrashHandler {

    init {
        System.loadLibrary("crash-handler")
    }

    external fun init(dumpPath: String)

    external fun testCrash()
}