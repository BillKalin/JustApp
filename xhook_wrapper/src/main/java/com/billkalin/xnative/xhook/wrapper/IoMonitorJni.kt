package com.billkalin.xnative.xhook.wrapper

class IoMonitorJni {

    init {
        System.loadLibrary("io-monitor")
    }

    external fun doHook()
}