package com.billkalin.performance.core.block

import android.os.Looper

object BlockChecker {

    private var blockMonitor: BlockMonitor? = null

    fun start() {
        blockMonitor?.shutdown()
        blockMonitor =
            BlockMonitor()
        Looper.getMainLooper().setMessageLogging(blockMonitor)
    }

    fun stop() {
        Looper.getMainLooper().setMessageLogging(null)
        blockMonitor?.shutdown()
    }
}

