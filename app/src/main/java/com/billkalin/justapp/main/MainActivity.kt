package com.billkalin.justapp.main

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.billkalin.hook.HookUtils
import com.billkalin.justapp.JustApp
import com.billkalin.justapp.R
import com.billkalin.justapp.bundle.DeviceFeature
import com.billkalin.justapp.crash.CrashCatcher
import com.billkalin.justapp.fix.QZoneHotfix
import com.billkalin.open.api.NativeOpenApi
import com.billkalin.open.api.OpenApi
import com.billkalin.xnative.xhook.wrapper.IoMonitorJni
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.UnsupportedOperationException
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipFile
import kotlin.system.measureTimeMillis


class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    private val DEVICE_FEATURE = "feature_device"
    private lateinit var splitManager: SplitInstallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        open_api.setOnClickListener(this)
        call_api.setOnClickListener(this)
        hook_api.setOnClickListener(this)
        acc_float.setOnClickListener(this)
        io_monitor_btn.setOnClickListener(this)
        io_monitor_init_btn.setOnClickListener(this)
        bundle_feature.setOnClickListener(this)
        install_crash_handler.setOnClickListener(this)
        uninstall_crash_handler.setOnClickListener(this)
        crash_handler.setOnClickListener(this)
        start_single_task.setOnClickListener(this)
        load_apk.setOnClickListener(this)
        write_info_to_apk.setOnClickListener(this)
        hot_fix.setOnClickListener(this)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.open_api -> {
                val result = false//OpenApi.open(false)
                getSomeThings()
                Toast.makeText(
                    this,
                    if (result) "open hidden api success!" else "open hidden api failed!! result = $result",
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.id.call_api -> {
                if (callHiddenApi()) {
                    Toast.makeText(
                        this,
                        "call hidden api success!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            R.id.hook_api -> {
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
            R.id.acc_float -> {
                FloatAccessibilityService.showFloatWindow(null)
            }
            R.id.io_monitor_init_btn -> {
                IoMonitorJni().doHook()
            }
            R.id.io_monitor_btn -> {
                val file = File(filesDir, "text.txt").apply {
                    if (!exists()) {
                        appendText("test text")
                    }
                }
                val texts = file.readText()
            }
            R.id.bundle_feature -> {
                launchAndInstallFeature(DEVICE_FEATURE)
            }
            R.id.install_crash_handler -> {
                CrashCatcher.install(Thread.UncaughtExceptionHandler { t, e ->
                    e.printStackTrace()
                    Toast.makeText(this, "crash catched please see the logcat!!", Toast.LENGTH_LONG)
                        .show()
                })
            }
            R.id.uninstall_crash_handler -> {
                CrashCatcher.unInstall()
            }
            R.id.crash_handler -> {
                throw IllegalStateException("just test crash catcher !!")
            }
            R.id.start_single_task -> {
                //连续点击触发
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    //正常启动时，只会启动一个Activity
//                    startActivity(Intent().apply {
//                        component = ComponentName(this@MainActivity, SingleTaskActivity::class.java)
//                    })
                    //会变成standard模式启动
                    startActivityForResult(Intent().apply {
                        component = ComponentName(this@MainActivity, SingleTaskActivity::class.java)
                    }, 15000)
                }, 1500L)
            }
            R.id.load_apk -> {
                loadApk()
            }
            R.id.write_info_to_apk -> {

            }
            R.id.hot_fix -> {
                QZoneHotfix.enableHotFix(this)
                QZoneHotfix.fix(JustApp.instance)
                Toast.makeText(
                    this,
                    "hot fix dex parch success, please click the SingleTask Activity button.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadApk() {
        MainScope().launch {
            val apkCachePath = File(cacheDir, "apk").absolutePath
            val apkPath = copyApk(apkCachePath)
            val dexDir = File(cacheDir, "dexs").apply { if (!exists()) mkdirs() }
            val optimizeDexDir = File(cacheDir, "dex_optimized").apply { if (!exists()) mkdirs() }
            loadDex(apkPath, dexDir.absolutePath, optimizeDexDir.absolutePath)
            patchResources(apkPath)
        }
    }

    private suspend fun copyApk(apkPath: String): String = withContext(Dispatchers.IO) {
        val cacheDir = File(apkPath)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val retFilePath = File(cacheDir, "source.apk")
        retFilePath.outputStream().use { fos ->
            assets.open("test.apk").use {
                var len = -1
                val buff = ByteArray(4 * 1024)
                len = it.read(buff)
                while (len != -1) {
                    fos.write(buff, 0, len)
                    len = it.read(buff)
                }
            }
        }
        retFilePath.absolutePath
    }

    private suspend fun loadDex(apkPath: String, dexFileDir: String, optimizeDexDir: String) =
        withContext(Dispatchers.IO) {
            val zipFile = ZipFile(apkPath)
            val dexFilesEntry =
                zipFile.entries().asSequence().filter { it.name.endsWith(".dex") }.toList()
            dexFilesEntry.forEach { zipEntry ->
                File(dexFileDir, zipEntry.name).outputStream().use { fos ->
                    zipFile.getInputStream(zipEntry).use {
                        var len: Int
                        val buff = ByteArray(4 * 1024)
                        len = it.read(buff)
                        while (len != -1) {
                            fos.write(buff, 0, len)
                            len = it.read(buff)
                        }
                    }
                }
            }
            val dexFiles = File(dexFileDir).listFiles().filter {
                it.isFile
            }.toList()
            installDex(dexFiles, optimizeDexDir)
        }

    private fun installDex(dexFiles: List<File>, optimizeDexDir: String) {
        val dexPathListField = getField(classLoader, "pathList")
        dexPathListField ?: return


        /*val newElements2 = dexFiles.mapNotNull {
            val dcl = DexClassLoader(it.absolutePath, optimizeDexDir, null, classLoader)
            val dexPath = dexPathListField.get(dcl)
            val oldElementsField = getField(dexPath, "dexElements")
            val oldElements = oldElementsField?.get(dexPath)
            oldElements
        }.toTypedArray().let {
            val combinedElements =
                java.lang.reflect.Array.newInstance(
                    elementClass,
                    (oldElements.size + newElements2.size)
                )
        }*/

        val dexPath = dexPathListField.get(classLoader)
        ///  private static Element[] makeDexElements(List<File> files, File optimizedDirectory,
        //            List<IOException> suppressedExceptions, ClassLoader loader) {
        val makeElementMethod = getMethod(
            dexPath,
            "makeDexElements",
            List::class.java,
            File::class.java,
            List::class.java,
            ClassLoader::class.java
        )
        makeElementMethod ?: return
        val exceptions = ArrayList<IOException>()
        val newElements = makeElementMethod.invoke(
            dexPath,
            dexFiles,
            File(optimizeDexDir),
            exceptions,
            classLoader
        ) as Array<*>
        val oldElementsField = getField(dexPath, "dexElements")
        oldElementsField ?: return
        val oldElements = oldElementsField.get(dexPath) as Array<*>
        val elementClass = oldElements::class.java.componentType
        val combinedElements =
            java.lang.reflect.Array.newInstance(
                elementClass,
                (oldElements.size + newElements.size)
            )
        System.arraycopy(oldElements, 0, combinedElements, 0, oldElements.size)
        System.arraycopy(
            newElements,
            0,
            combinedElements,
            oldElements.size,
            newElements.size
        )
        oldElementsField.set(dexPath, combinedElements)
    }


    private fun patchResources(apkPath: String) {
        val asserss = AssetManager::class.java.newInstance()
        val addAssetPathMethod = getMethod(asserss, "addAssetPath", String::class.java)
        addAssetPathMethod ?: return
        val ret = addAssetPathMethod.invoke(asserss, apkPath) as Int
        if (ret > 0) {
            val pms = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES)
            val res = Resources(asserss, resources.displayMetrics, resources.configuration)
            val id =
                res.getIdentifier("rv_item_app_inco", "layout", pms.packageName)
            if (id > 0) {
                val layout = LayoutInflater.from(this).inflate(id, null, false)
            }
        }
    }

    private fun getField(instance: Any, fieldName: String): Field? {
        var cls = instance.javaClass
        while (cls != null) {
            try {
                val f = cls.getDeclaredField(fieldName)
                if (!f.isAccessible) {
                    f.isAccessible = true
                }
                return f
            } catch (e: NoSuchFieldException) {

            }
            cls = cls.superclass as Class<Any>
        }
        return null
    }

    private fun getMethod(
        instance: Any,
        methodName: String,
        vararg params: Class<out Any>
    ): Method? {
        var cls = instance::class.java
        while (cls != null) {
            try {
                val method = cls.getDeclaredMethod(methodName, *params)
                if (!method.isAccessible) {
                    method.isAccessible = true
                }
                return method
            } catch (e: NoSuchMethodException) {

            }
            cls = cls.superclass as Class<out Any>
        }
        return null
    }

    private fun writeInfoToApk(apkPath: String) {
        val file = File(apkPath)
        val zipFile = ZipFile(apkPath)
        val comments = zipFile.comment
        val commentsSize = if (comments.isNullOrEmpty()) {
            0
        } else {
            comments.toByteArray().size
        }
        val endOfCentralDirectoryLength = commentsSize + 22
        val endOfCentralDirectoryOffset = file.length() - endOfCentralDirectoryLength
        val pointer = endOfCentralDirectoryOffset + 16
        val centralDirectoryOffset = file.readData(pointer, 4).toInt()
        val v2SignBlockOffset = centralDirectoryOffset - 16
        val magicBytes = file.readData((v2SignBlockOffset).toLong(), 16)
        val magic = String(magicBytes)
        if ("APK Sig Block 42" != magic) {
            //不包含v2签名信息
            return
        }
        val signInfoOffset = v2SignBlockOffset - 8
        val signBlockSize = file.readData(signInfoOffset.toLong(), 8).toInt()
        val signInfoStartOffset = signInfoOffset - signBlockSize + 16

    }

    private fun buildInfo(id: Int, values: ByteArray): ByteArray {
        val idBytes = id.toLittleEndianBytes()
        val idSize = (4 + values.size).toLong()
        val valueSize = idSize.toLittleEndianBytes()

        val retData = ByteArray(8 + 4 + values.size)
        //ID-value大小 = 4字节value大小 + Value大小，12个字节
        System.arraycopy(valueSize, 0, retData, 0, 8)
        //ID值 4个字节
        System.arraycopy(idBytes, 0, retData, 8, 4)
        //value值 任意大小
        System.arraycopy(values, 0, retData, 12, values.size)

        return retData
    }

    private fun Number.toLittleEndianBytes(): ByteArray {
        when (this) {
            is Byte -> {
                return ByteBuffer.allocate(1)
                    .order(ByteOrder.LITTLE_ENDIAN).put(this).array()
            }

            is Short -> {
                return ByteBuffer.allocate(2)
                    .order(ByteOrder.LITTLE_ENDIAN).putShort(this).array()
            }
            is Int -> {
                return ByteBuffer.allocate(4)
                    .order(ByteOrder.LITTLE_ENDIAN).putInt(this).array()
            }
            is Long -> {
                return ByteBuffer.allocate(8)
                    .order(ByteOrder.LITTLE_ENDIAN).putLong(this).array()
            }
            is Float -> {
                return ByteBuffer.allocate(4)
                    .order(ByteOrder.LITTLE_ENDIAN).putFloat(this).array()
            }
            is Double -> {
                return ByteBuffer.allocate(8)
                    .order(ByteOrder.LITTLE_ENDIAN).putDouble(this)
                    .array()
            }
        }
        throw UnsupportedOperationException("not support ${this.javaClass.simpleName}")
    }

    private fun ByteArray.toInt(): Int {
        return (this[1].toInt() shl 8) or (this[0].toInt() and 0xFF)
    }

    private fun File.readData(offset: Long, len: Int): ByteArray {
        var ret: ByteArray
        FileInputStream(this).use {
            it.skip(offset)
            val buff = ByteArray(4)
            it.read(buff, 0, len)
            ret = buff
        }
        return ret
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(
            TAG,
            "onActivityResult: requestCode = $requestCode, resultCode = $resultCode, data = $data"
        )
    }
}