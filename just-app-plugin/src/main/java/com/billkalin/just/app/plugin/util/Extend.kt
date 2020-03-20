package com.billkalin.just.app.plugin.util

import java.io.File

fun File.eachFileRecurse(callback: (file: File) -> Unit) {
    if(this.isDirectory) {
        listFiles()?.forEach {
            if(it.isDirectory) {
                it.eachFileRecurse(callback)
            } else {
                callback(it)
            }
        }
    } else {
        callback(this)
    }
}