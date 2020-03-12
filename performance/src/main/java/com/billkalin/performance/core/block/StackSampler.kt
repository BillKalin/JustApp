package com.billkalin.performance.core.block

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.text.TextUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.LinkedHashMap

class StackSampler {

    companion object {
        private const val SAMPLER_DURATION = 300L
        private const val DEFAULT_MAX_ENTRY_COUNT = 100
        private const val SEPARATOR = "\r\n"
        private val TIME_FORMATTER: SimpleDateFormat =
            SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.CHINESE)
    }

    private var mHandler: Handler
    private val mHandlerThread: HandlerThread = HandlerThread("StackSampler")
    private val isStart = AtomicBoolean(false)
    private val sStackMap: LinkedHashMap<Long, String> = LinkedHashMap()
    private var dumpRunnable: Runnable? = null
    private var mCacheStackStr: String = ""

    init {
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
        dumpRunnable = Runnable {
            doDumpInfo()
            if (isStart.get()) {
                mHandler.postDelayed(dumpRunnable,
                    SAMPLER_DURATION
                )
            }
        }
    }

    fun startDump() {
        if (isStart.get())
            return
        isStart.set(true)
        mHandler.removeCallbacks(dumpRunnable)
        mHandler.postDelayed(dumpRunnable,
            SAMPLER_DURATION
        )
    }

    fun stopDump() {
        if (!isStart.get())
            return
        isStart.set(false)
        mHandler.removeCallbacks(dumpRunnable)
    }

    fun shutdown() {
        stopDump()
        mHandlerThread.quitSafely()
    }

    private fun doDumpInfo() {
        val stackString = StringBuilder()
        Looper.getMainLooper().thread.stackTrace.forEach {
            stackString.append(it.toString()).append(SEPARATOR)
        }

        synchronized(sStackMap) {
            if (sStackMap.size == DEFAULT_MAX_ENTRY_COUNT) {
                sStackMap.remove(sStackMap.keys.iterator().next())
            }
            if (!needIgnore(stackString.toString())) {
                sStackMap[System.currentTimeMillis()] = stackString.toString()
            }
        }
    }

    private fun needIgnore(string: String): Boolean {
        if (TextUtils.equals(mCacheStackStr, string))
            return true
        mCacheStackStr = string
        return false
    }

    fun getThreadStackInfo(startTime: Long, endTime: Long): List<String> {
        val ret = mutableListOf<String>()
        synchronized(sStackMap) {
            sStackMap.filter {
                it.key in (startTime + 1) until endTime
            }.forEach {
                val entryTime = it.key
                val stack = it.value
                ret.add(
                    TIME_FORMATTER.format(entryTime)
                            + SEPARATOR
                            + SEPARATOR
                            + stack
                );

            }
        }
        return ret
    }
}