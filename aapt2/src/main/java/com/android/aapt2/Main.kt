package com.android.aapt2

import java.io.File

fun main(vararg args: String) {
    val f = String.javaClass.classLoader.getResource("debug").file
    File(f).listFiles()?.filter { file ->
        file.name.endsWith(".png.flat") && (file.name.length < 11 || !file.name.regionMatches(
            file.name.length - 11,
            ".9",
            0,
            2,
            true
        ))
    }?.forEach {
        val metaData = it.metaData
        println("relatePath = ${metaData.relatePath} " + metaData.toString())
    }
}

val MetaData.relatePath: String
    get() {
        val file = File(this.resourcePath)
        return file.parentFile.name + File.separator + file.name
    }