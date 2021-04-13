package com.billkalin.justapp.util

import dalvik.system.DexClassLoader
import java.io.File
import java.security.MessageDigest

object IoUtils {

    @JvmStatic
    fun File.checkSum(): ByteArray {
        if (this.isDirectory || !this.exists())
            return ByteArray(0)
        val messageDigest = MessageDigest.getInstance("MD5")
        val buff = ByteArray(4 * 1024)
        this.inputStream().use {
            while (true) {
                val len = it.read(buff)
                if (len == -1)
                    break
                messageDigest.update(buff, 0, len)
            }
        }
        return messageDigest.digest()
    }

    @JvmStatic
    fun inject(classLoader: ClassLoader) {

    }

    private fun setParent(classLoader: ClassLoader, parent: ClassLoader) {
        val field = ClassLoader::class.java.getDeclaredField("parent")
        field.isAccessible = true
        field.set(classLoader, parent)
    }

    class IncrementClassLoader(
        dexPath: String,
        optmizeDir: String,
        nativeSearchPath: String,
        parent: ClassLoader
    ) : DexClassLoader(dexPath, optmizeDir, nativeSearchPath, parent) {

    }
}