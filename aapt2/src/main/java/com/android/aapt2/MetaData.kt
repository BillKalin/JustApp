package com.android.aapt2

import com.android.aapt2.proto.ConfigurationOuterClass

class MetaData(
    val resourceName: String,
    val resourcePath: String,
    val config: ConfigurationOuterClass.Configuration?
) {
    override fun toString(): String {
        return "MetaData(resourceName='$resourceName', resourcePath='$resourcePath', config=${config?.serializedSize ?: 0})"
    }
}