package com.billkalin.hook

import android.os.Handler
import android.os.Message
import android.util.Log
import com.billkalin.kreflect.KQuietReflect

class HandlerCallbackProxy(handler: Handler, callback: Handler.Callback?) : Handler.Callback {

    private val mHandler = handler
    private val mCallback = callback

    companion object {
        const val TAG = "HandlerCallbackProxy"

        /**
         * Android 28开始 变量从110开始
         */
        private const val LAUNCH_ACTIVITY = 100
        private const val PAUSE_ACTIVITY = 101
        private const val EXECUTE_TRANSACTION = 159
        private const val LAUNCH_ITEM_CLASS = "android.app.servertransaction.ResumeActivityItem"
        private const val PAUSE_ITEM_CLASS = "android.app.servertransaction.PauseActivityItem"
    }

    override fun handleMessage(msg: Message?): Boolean {
        val what = preHandle(msg)
        if (mCallback != null && mCallback.handleMessage(msg)) {
            afterHandle(what)
            return true
        }
        mHandler.handleMessage(msg)
        afterHandle(what)
        return true
    }

    private fun preHandle(msg: Message?): Int {
        msg ?: return -1
        val what = msg.what
        when (what) {
            LAUNCH_ACTIVITY -> {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "preHandle: LAUNCH_ACTIVITY")
                }
            }
            PAUSE_ACTIVITY -> {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "preHandle: PAUSE_ACTIVITY")
                }
            }
            EXECUTE_TRANSACTION -> {
                return handleAboveP(msg)
            }
        }
        return what
    }

    private fun afterHandle(what: Int) {
        when (what) {
            LAUNCH_ACTIVITY -> {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "afterHandle: LAUNCH_ACTIVITY")
                }
            }
            PAUSE_ACTIVITY -> {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "afterHandle: PAUSE_ACTIVITY")
                }
            }
        }
    }

    private fun handleAboveP(msg: Message): Int {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "handleAboveP: msg = $msg")
        }
        val obj = msg.obj
        val activityCallback =
            KQuietReflect.with(obj).method("getLifecycleStateRequest").call<Any>()
        activityCallback?.let {
            val clsName = it::class.java.canonicalName
            if (LAUNCH_ITEM_CLASS == clsName) {
                return LAUNCH_ACTIVITY
            } else if (PAUSE_ITEM_CLASS == clsName) {
                return PAUSE_ACTIVITY
            }
        }
        return msg.what
    }
}