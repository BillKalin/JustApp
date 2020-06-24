package com.billkalin.justapp.main

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Context
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import com.billkalin.justapp.BuildConfig

private const val TAG = "FloatAccService"

class FloatAccessibilityService : AccessibilityService() {


    companion object {
        private lateinit var mService: Context
        private val wms: WindowManager
            get() = mService.getSystemService(Service.WINDOW_SERVICE) as WindowManager

        private var mView: View? = null

        fun showFloatWindow(context: Context?) {
            mService = context ?: mService
            if (mView == null) {
                mView = Button(mService).apply {
                    width = 200
                    height = 200
                    text = "Float Window"
                    setOnClickListener {
//                        hideFloatWindow()
                    }
                }
            }

            val layParams = WindowManager.LayoutParams().apply {
                type =
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY// WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                flags =
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            mView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)

            wms.addView(mView, layParams)
        }

        fun hideFloatWindow() {
            wms.removeView(mView)
        }
    }

    override fun onInterrupt() {

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onServiceConnected")
        }
        mService = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onAccessibilityEvent: $event")
        }
    }
}