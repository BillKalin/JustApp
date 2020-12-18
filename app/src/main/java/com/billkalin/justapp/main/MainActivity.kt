package com.billkalin.justapp.main

import android.app.Activity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billkalin.hook.HookUtils
import com.billkalin.justapp.R
import com.billkalin.open.api.NativeOpenApi
import com.billkalin.open.api.OpenApi
import com.billkalin.xnative.xhook.wrapper.IoMonitor
import com.billkalin.xnative.xhook.wrapper.IoMonitorJni
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Thread.sleep(5000L)

        open_api.setOnClickListener {
            val result = false//OpenApi.open(false)
            getSomeThings()
            Toast.makeText(
                this,
                if (result) "open hidden api success!" else "open hidden api failed!! result = $result",
                Toast.LENGTH_SHORT
            ).show()
        }

        call_api.setOnClickListener {
            if (callHiddenApi()) {
                Toast.makeText(
                    this,
                    "call hidden api success!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        hook_api.setOnClickListener {
            val result = OpenApi.open(false)
            if (result) {
                HookUtils.hookActivityInstrumentation()
            } else {
                Toast.makeText(
                    this,
                    if (result) "open hidden api success!" else "open hidden api failed!! result = $result",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        acc_float.setOnClickListener {
            FloatAccessibilityService.showFloatWindow(null)
        }

        IoMonitorJni().doHook()

        io_monitor_btn.setOnClickListener {
//            Thread(Runnable {
                val file = File(filesDir, "text.txt")
//                file.writeText("texttextxt")

                val texts = file.readText()
//            }).start()
//            android.os.Handler(Looper.getMainLooper()).postDelayed({
//
//
//            }, 2000L)

        }
    }

    private fun callHiddenApi(): Boolean {
        try {
            val method = Activity::class.java.getDeclaredMethod("canStartActivityForResult")
            val ret = method.invoke(this)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    private suspend fun doFiles() = withContext(Dispatchers.IO) {
        Thread.sleep(6000)
    }

    private suspend fun doFiles2() = withContext(Dispatchers.IO) {
        Thread.sleep(5000)
    }

    private fun getSomeThings() {

        NativeOpenApi.openJdwp(true)

        GlobalScope.launch(Dispatchers.Main) {
            val times = measureTimeMillis {
                val ret = async { doFiles() }
                Log.d(TAG, "times = 1")
                val ret1 = doFiles2()
                Log.d(TAG, "times = 2, ")
            }
            Log.d(TAG, "times = $times, ${Thread.currentThread().name}")
        }
    }
}

class MyViewModel : ViewModel() {

    fun test() {
        viewModelScope.launch {

        }
    }

}