package com.billkalin.justapp.fix

import android.content.Context
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexClassLoader
import java.io.File

class HotFixUtils {


    fun fix(context: Context) {
        val classLoader = context.classLoader
        if (classLoader is BaseDexClassLoader) {
            val dexPathList = classLoader::class.java.getField2("dexPathList", classLoader)
            val dexPath = ""
            val dexOptimizeDir = ""
            val librarySearchPath = ""
            val dexClassLoader = DexClassLoader(dexPath, dexOptimizeDir, librarySearchPath,classLoader)
            val newDexPath = dexClassLoader::class.java.getField2("dexPathList", classLoader)

            val params = arrayOf(List::class.java, File::class.java,List::class.java, ClassLoader::class.java)
            val method = dexPathList::class.java.getDeclaredMethod("makeDexElements", *params).apply {
                isAccessible= true
            }
        }
    }


//  private static Element[] makeDexElements(List<File> files, File optimizedDirectory,
//            List<IOException> suppressedExceptions, ClassLoader loader) {
//        return makeDexElements(files, optimizedDirectory, suppressedExceptions, loader, false);
//    }
    private fun Class<*>.getField2(fileName: String, obj: Any): Any {
        val field = this::class.java.getDeclaredField(fileName).apply {
            isAccessible = true
        }
        return field.get(obj)
    }

}