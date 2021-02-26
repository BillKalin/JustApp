package com.billkalin.justapp.main

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.billkalin.hook.HookUtils
import com.billkalin.justapp.JustApp
import com.billkalin.justapp.R
import com.billkalin.justapp.bundle.DeviceFeature
import com.billkalin.open.api.NativeOpenApi
import com.billkalin.open.api.OpenApi
import com.billkalin.xnative.xhook.wrapper.IoMonitorJni
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    private val DEVICE_FEATURE = "feature_device"
    private lateinit var splitManager: SplitInstallManager

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
            val file = File(filesDir, "text.txt").apply {
                if (!exists()) {
                    appendText("test text")
                }
            }
            val texts = file.readText()
        }

        bundle_feature.setOnClickListener {
            launchAndInstallFeature(DEVICE_FEATURE)
        }

        splitManager = SplitInstallManagerFactory.create(this).apply {
            registerListener(installListener)
        }
    }

    private val installListener = SplitInstallStateUpdatedListener { state ->
        when (state.status()) {
            SplitInstallSessionStatus.INSTALLED -> {

            }
            SplitInstallSessionStatus.FAILED -> {

            }
            SplitInstallSessionStatus.DOWNLOADED -> {

            }
            SplitInstallSessionStatus.INSTALLING -> {

            }
            SplitInstallSessionStatus.CANCELED -> {

            }
            SplitInstallSessionStatus.CANCELING -> {

            }
            SplitInstallSessionStatus.DOWNLOADING -> {

            }
            SplitInstallSessionStatus.PENDING -> {

            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {

            }
            SplitInstallSessionStatus.UNKNOWN -> {

            }
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

    private fun launchAndInstallFeature(featureName: String) {
        //已安装该模块
        if (splitManager.installedModules.contains(featureName)) {
            Log.d(TAG, "launchAndInstallFeature = $featureName is installed")
            getDeviceFeature()?.apply {
                initFeature(JustApp.instance)
                val model = getDeviceModel()
                val pkg = getPackageName()
                Log.d(TAG, "launchAndInstallFeature model = $model is pkg = $pkg")
            }
            val intent = Intent().apply {
                component = ComponentName(
                    packageName,
                    "com.billkalin.feature.device.DeviceActivity"
                )
            }
            startActivity(intent)
            return
        }
        val installRequest = SplitInstallRequest.newBuilder().addModule(featureName).build()
        splitManager.startInstall(installRequest).addOnSuccessListener {
            /* getDeviceFeature()?.apply {
                 initFeature(JustApp.instance)
                 val model = getDeviceModel()
                 val pkg = getPackageName()
                 Log.d(TAG, "launchAndInstallFeature addOnSuccessListener: model = $model is pkg = $pkg")
             }*/
            val intent = Intent().apply {
                component = ComponentName(
                    packageName,
                    "com.billkalin.feature.device.DeviceActivity"
                )
            }
            startActivity(intent)
        }.addOnFailureListener {
            Log.d(TAG, "launchAndInstallFeature addOnFailureListener: error = $it")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        splitManager.unregisterListener(installListener)
    }

    private fun getDeviceFeature(): DeviceFeature? {
        return Class.forName("com.billkalin.feature.device.DeviceFeatureImpl")
            .newInstance() as? DeviceFeature
    }
}