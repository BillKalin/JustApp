package com.base.app.spi.transformer

import java.io.File

interface ArtifactManager {

    companion object {
        const val AAR = "AAR"
        const val ALL_CLASSES = "ALL_CLASSES"
        const val APK = "APK"
        const val JAR = "JAR"
        const val JAVAC = "JAVAC"
        const val MERGED_ASSETS = "MERGED_ASSETS"
        const val MERGED_RES = "MERGED_RES"
        const val MERGED_MANIFESTS = "MERGED_MANIFESTS"
        const val PROCESSED_RES = "PROCESSED_RES"
        const val SYMBOL_LIST = "SYMBOL_LIST"
        const val SYMBOL_LIST_WITH_PACKAGE_NAME = "SYMBOL_LIST_WITH_PACKAGE_NAME"
        const val DATA_BINDING_DEPENDENCY_ARTIFACTS = "DATA_BINDING_DEPENDENCY_ARTIFACTS"
    }

    fun get(type: String): Collection<File> = emptyList()
}