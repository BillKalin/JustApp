
package com.billkalin.justapp.crash

import android.content.Context
import dalvik.system.BaseDexClassLoader
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method

object Inject {

    private const val DEX_OPT_DIR = "dex_opt"

    @JvmStatic
    fun inject(ctx: Context, dexFiles: List<String>) {
        if (dexFiles.isNullOrEmpty())
            return
        val classLoader = if (ctx.classLoader is BaseDexClassLoader) {
            ctx.classLoader
        } else {
            Class.forName("dalvik.system.BaseDexClassLoader")
        }
        val dexDirPath = File(ctx.applicationContext.filesDir, DEX_OPT_DIR)
        if (dexDirPath.exists()) {
            dexDirPath.delete()
        }

        val dexPathList = classLoader::class.java.getField("dexPathList", classLoader)
        val oldElements = dexPathList::class.java.getField("dexElements", dexPathList)

        val parameterName = arrayOf(List::class.java, File::class.java, List::class.java, ClassLoader::class.java)
        val makeElementsMethod = dexPathList::class.java.getMethod2("makeDexElements", *parameterName)
        val optDir = dexDirPath
        val exceptions = ArrayList<IOException>()
        val cls = classLoader
        val newElements = makeElementsMethod.invoke(dexPathList, dexFiles, optDir, exceptions, cls) as? Array<Any>

        val finalElement = combineElements(oldElements as Array<Any>, newElements as Array<Any>)
        dexPathList::class.java.setField(dexPathList, "dexElements", finalElement)
    }

    private fun combineElements(old: Array<Any>, new: Array<Any>): Array<Any> {
        val final = Array<Any>(old.size + new.size) {}
        System.arraycopy(new, 0, final, 0, new.size)
        System.arraycopy(old, 0, final, new.size, old.size)
        return final
    }

    private fun Class<*>.getMethod2(name: String, vararg parameterName: Class<*>): Method {
        var cls = this ?: null
        while (cls != null) {
            val method = try {
                getDeclaredMethod(name, *parameterName).apply { isAccessible = true }
            } catch (e: NoSuchMethodException) {
                null
            } catch (e: SecurityException) {
                null
            }
            if (method != null)
                return method
            cls = this.superclass as? Class<*>
        }
        throw NoSuchMethodException("${this::class.java.name} has no method $name($parameterName) }")
    }

    private fun Class<*>.getField(name: String, obj: Any): Any {
        val field = getFieldFully(name)
        return field.get(obj)
    }

    private fun Class<*>.getFieldFully(name: String): Field {
        var cls = this ?: null
        while (cls != null) {
            val field = try {
                cls.getDeclaredField(name).apply { isAccessible = true }
            } catch (e: NoSuchFieldException) {
                null
            }
            if (field != null)
                return field
            cls = this.superclass as? Class<*>
        }
        throw NoSuchFieldException("${this::class.java.canonicalName} no field $name")
    }


    private fun Class<*>.setField(obj: Any, name: String, value: Any) {
        val field = getFieldFully(name)
        field.set(obj, value)
    }
}