package com.billkalin.performance.core.block

import android.util.Log
import android.util.Printer
import com.billkalin.performance.BuildConfig

class BlockMonitor : Printer {

    var isStart = false
    var startTime = 0L

    private val mStackSampler =
        StackSampler()

    companion object {
        const val MAX_BLOCK_TIME = 200L
    }

    override fun println(x: String?) {
        isStart = !isStart
        if (isStart) {
            startTime = System.currentTimeMillis()
            mStackSampler.startDump()
        } else {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            if (duration >= MAX_BLOCK_TIME) {
                val info = mStackSampler.getThreadStackInfo(startTime, endTime)
                if (BuildConfig.DEBUG) {
                    Log.e(
                        BlockMonitor::class.java.simpleName,
                        "printStackTrace: $info"
                    )
                }
            }
            mStackSampler.stopDump()
        }
    }

    fun shutdown() {
        mStackSampler.shutdown()
    }
}