package com.just.app.permission.check.plugin

import JustApp.permission.check.plugin.Build
import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.base.app.spi.transformer.ArtifactManager
import com.base.app.spi.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.PrintWriter
import javax.xml.parsers.SAXParserFactory

open class PermissionTask : DefaultTask() {

    lateinit var variant: BaseVariant
    private val sAXParserFactory: SAXParserFactory = SAXParserFactory.newInstance()
    private var logger: PrintWriter? = null

    init {
        sAXParserFactory.isValidating = false
        sAXParserFactory.isNamespaceAware = true
        sAXParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
        sAXParserFactory.setFeature("http://xml.org/sax/features/xmlns-uris", true)
    }

    @TaskAction
    open fun run() {
        if (logger == null) {
            logger =
                File(variant.project.buildDir, "reports").also { it.mkdirs() }
                    .file(Build.ARTIFACT)
                    .file(variant.name).file("permission.txt").touch().printWriter()
        }
        variant.artifacts.get(ArtifactManager.MERGED_MANIFESTS).forEach {
            it.search { file ->
                file.name == SdkConstants.FN_ANDROID_MANIFEST_XML
            }.forEach { file ->
                PermissionHandler().also { handler ->
                    sAXParserFactory.newSAXParser().parse(file, handler)
                }.permissions.forEach { permission ->
                    logger?.println(permission)
                }
                logger?.close()
            }
        }
    }
}