package com.just.app.r.inline.plugin

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.base.app.spi.transformer.ArtifactManager
import com.base.app.spi.util.artifacts
import com.base.app.spi.util.search
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.xml.parsers.SAXParserFactory

open class PermissionTask : DefaultTask() {

    lateinit var variant: BaseVariant
    private val sAXParserFactory: SAXParserFactory = SAXParserFactory.newInstance()

    init {
        sAXParserFactory.isValidating = false
        sAXParserFactory.isNamespaceAware = true
        sAXParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
        sAXParserFactory.setFeature("http://xml.org/sax/features/xmlns-uris", true)
    }

    @TaskAction
    open fun run() {
        variant.artifacts.get(ArtifactManager.MERGED_MANIFESTS).forEach {
            it.search { file ->
                file.name == SdkConstants.FN_ANDROID_MANIFEST_XML
            }.forEach {
                sAXParserFactory.newSAXParser().parse(it, PermissionHandler())
            }
        }
    }
}