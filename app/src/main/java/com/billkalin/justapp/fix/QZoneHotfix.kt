package com.billkalin.justapp.fix

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method

object QZoneHotfix {

    private var fixed: Boolean = false
    private lateinit var sharePreference: SharedPreferences
    private const val HOF_FIX_PATCHED = "hof_fix_patched"
    private const val PATCH_DEX_DIR = "patch_dex"
    private const val HOT_FIX_FILE_NAME = "patch.dex"
    private const val FIX_FILE_NAME = "fix.dex"

    @JvmStatic
    fun enableHotFix(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(context, "QZoneHotfix just apply below Android L", Toast.LENGTH_SHORT).show()
            return
        }
        sharePreference = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        sharePreference.edit().putBoolean(HOF_FIX_PATCHED, true).apply()
    }

    /**
     * 由于Android 5.0+以后的虚拟机改完art，因此这个修复方式只适用于Android 5.0以下
     */
    @JvmStatic
    fun fix(app: Application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(app, "QZoneHotfix just apply below Android L", Toast.LENGTH_SHORT).show()
            return
        }
        sharePreference = app.getSharedPreferences(app.packageName, Context.MODE_PRIVATE)
        fixed = sharePreference.getBoolean(HOF_FIX_PATCHED, false)
        if (!fixed)
            return

        val classLoader = app.classLoader
        val dexPathList = classLoader.field("pathList")
        dexPathList ?: return
        val optDir = File(app.filesDir, PATCH_DEX_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val files = getPatchDex(app, optDir)
        val odexDir = File(optDir, "odex").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val newElements = when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.M -> {
                VLast.makeElements(dexPathList, files, odexDir, app.classLoader)
            }
            Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT -> {
                V23.makeElements(dexPathList, files, odexDir)
            }
            else -> {
                V19.makeElements(dexPathList, ArrayList(files), odexDir)
            }
        }
        val oldElements = dexPathList.field("dexElements") as Array<Any>
        val elementsClass = oldElements::class.java.componentType
        val finalElements = newElements.add(elementsClass, oldElements)
        dexPathList.setField("dexElements", finalElements)

        Log.d(QZoneHotfix::class.java.simpleName, "fix success !!")
    }

    private fun getPatchDex(context: Context, file: File): List<File> {
        val dexFile = File(file, HOT_FIX_FILE_NAME)
        if (!dexFile.exists()) {
            dexFile.outputStream().use {
                context.assets.open(HOT_FIX_FILE_NAME).use { iStream ->
                    it.write(iStream.readBytes())
                }
            }
        }
        val fixFile = File(file, FIX_FILE_NAME)
        if (!fixFile.exists()) {
            fixFile.outputStream().use {
                context.assets.open(FIX_FILE_NAME).use { iStream ->
                    it.write(iStream.readBytes())
                }
            }
        }
        return arrayListOf(dexFile, fixFile)
    }

    private fun Any.field(name: String): Any? {
        val f = this.getFieldFully(name)
        return f.get(this)
    }

    private fun Any.method(name: String, vararg parameterName: Class<*>): Method {
        return this.getMethodFully(name, *parameterName)
    }

    private fun <T> Array<T>.add(componentType: Class<*>, array: Array<T>): Array<T> {
        val newArray = java.lang.reflect.Array.newInstance(componentType, this.size + array.size)
        System.arraycopy(this, 0, newArray, 0, this.size)
        System.arraycopy(array, 0, newArray, this.size, array.size)
        return newArray as Array<T>
    }

    private fun Any.getFieldFully(name: String): Field {
        var cls = this::class.java ?: null
        while (cls != null) {
            val field = try {
                cls.getDeclaredField(name).apply {
                    isAccessible = true
                }
            } catch (e: NoSuchFieldException) {
                null
            }
            if (field != null) {
                return field
            }
            cls = cls.superclass as? Class<out Class<*>>
        }
        throw NoSuchFieldException("${this::class.java.canonicalName} has no field $name")
    }

    private fun Any.getMethodFully(name: String, vararg params: Class<*>): Method {
        var cls = this::class.java ?: null
        while (cls != null) {
            val method = try {
                cls.getDeclaredMethod(name, *params).apply {
                    isAccessible = true
                }
            } catch (e: NoSuchMethodException) {
                null
            }
            if (method != null) {
                return method
            }
            cls = cls.superclass as? Class<out Class<*>>
        }
        throw NoSuchMethodException("${this::class.java.canonicalName} has no method $name")
    }

    private fun Any.setField(name: String, value: Any?) {
        val field = this.getFieldFully(name)
        field.set(this, value)
    }

    private object VLast {
        @JvmStatic
        fun makeElements(
            dexPathList: Any,
            files: List<File>,
            optDir: File,
            clssLoader: ClassLoader
        ): Array<Any> {
            val params = arrayOf(
                List::class.java,
                File::class.java,
                List::class.java,
                ClassLoader::class.java
            )
            val exceptions = mutableListOf<IOException>()
            val method = dexPathList.method("makeDexElements", *params)
            val elements =
                method.invoke(dexPathList, files, optDir, exceptions, clssLoader) as Array<Any>
            if (exceptions.isNotEmpty()) {
                exceptions.forEach {
                    Log.d(VLast::class.simpleName, "error: $it")
                }
            }
            return elements
        }
    }

    private object V23 {
        @JvmStatic
        fun makeElements(dexPathList: Any, files: List<File>, optDir: File): Array<Any> {
            val params = arrayOf(List::class.java, File::class.java, List::class.java)
            val exceptions = mutableListOf<IOException>()
            val makeDexElementsMethod = dexPathList.method("makePathElements", *params)
            val elements =
                makeDexElementsMethod.invoke(dexPathList, files, optDir, exceptions) as Array<Any>
            if (exceptions.isNotEmpty()) {
                exceptions.forEach {
                    Log.d(V23::class.simpleName, "error: $it")
                }
            }
            return elements
        }
    }

    private object V19 {
        @JvmStatic
        fun makeElements(dexPathList: Any, files: ArrayList<File>, optDir: File): Array<Any> {
            val params = arrayOf(ArrayList::class.java, File::class.java, ArrayList::class.java)
            val exceptions = mutableListOf<IOException>()
            val makeDexElementsMethod = dexPathList.method("makeDexElements", *params)
            val elements =
                makeDexElementsMethod.invoke(dexPathList, files, optDir, exceptions) as Array<Any>
            if (exceptions.isNotEmpty()) {
                exceptions.forEach {
                    Log.d(V19::class.simpleName, "error: $it")
                }
            }
            return elements
        }
    }
}
