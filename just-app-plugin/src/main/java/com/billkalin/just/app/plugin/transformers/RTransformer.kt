package com.billkalin.just.app.plugin.transformers

import com.android.SdkConstants
import com.android.SdkConstants.DOT_CLASS
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.billkalin.just.app.plugin.util.RUtils
import com.billkalin.just.app.plugin.util.eachFileRecurse
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class RTransformer(project: Project) : Transform() {

    private val project = project

    override fun getName(): String = "Rtransform"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    override fun isIncremental(): Boolean = false

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        TransformManager.SCOPE_FULL_PROJECT

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        val outputProvider = transformInvocation.outputProvider
        outputProvider.deleteAll()

        RUtils.reset()

        val inputs = transformInvocation.inputs
        val jarList = mutableListOf<File>()
        inputs.parallelStream().forEach {
            it.directoryInputs.parallelStream().forEach { input ->
                input.file.eachFileRecurse { file ->
                    println("directoryInputs: path = ${file.absolutePath}")
                    RUtils.collectClassFileInfo(file)
                }
            }
            it.jarInputs.parallelStream().forEach { jar ->
                var jarName = jar.name
                if (jarName.endsWith(SdkConstants.DOT_JAR, ignoreCase = true)) {
                    jarName = jarName.substring(0, jarName.length - SdkConstants.DOT_JAR.length)
                }
                val file = jar.file
                val jarFile = JarFile(file)
                val srcFile = File(file.parentFile, "temp.jar").apply {
                    if (exists()) {
                        delete()
                    }
                }
                val jos = JarOutputStream(FileOutputStream(srcFile))
                val entries = jarFile.entries()
                while (entries.hasMoreElements()) {
                    val jarEntry = entries.nextElement()
                    val zipEntry = ZipEntry(jarEntry.name)
                    val jis = jarFile.getInputStream(zipEntry)
                    jos.putNextEntry(zipEntry)
                    if (zipEntry.name.endsWith(DOT_CLASS, ignoreCase = true)) {
                        jos.write(RUtils.collectJarFileInfo(IOUtils.toByteArray(jis)))
                    } else {
                        jos.write(IOUtils.toByteArray(jis))
                    }
                    jos.closeEntry()
                }
                jos.close()

                val md5 = DigestUtils.md5Hex(file.absolutePath)
                val dst = outputProvider.getContentLocation(
                    jarName + md5,
                    jar.contentTypes,
                    jar.scopes,
                    Format.JAR
                )
                FileUtils.copyFile(srcFile, dst)
                jarList.add(dst)
            }
        }

        inputs.parallelStream().forEach {
            it.directoryInputs.parallelStream().forEach { input ->
                input.file.eachFileRecurse { file ->
                    RUtils.replaceRFileAndDelete(file)
                }
                val dst = outputProvider.getContentLocation(
                    input.name,
                    input.contentTypes,
                    input.scopes,
                    Format.DIRECTORY
                )
                FileUtils.copyDirectory(input.file, dst)
            }
            it.jarInputs.parallelStream().forEach { jarInput ->
                var jarName = jarInput.name
                if (jarName.endsWith(SdkConstants.DOT_JAR, ignoreCase = true)) {
                    jarName = jarName.substring(0, jarName.length - SdkConstants.DOT_JAR.length)
                }
                val file = jarInput.file

                val srcFile = RUtils.replaceAndDeleteRInfoFromJar(file)

                val md5 = DigestUtils.md5Hex(file.absolutePath)
                val dst = outputProvider.getContentLocation(
                    jarName + md5,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                FileUtils.copyFile(srcFile, dst)
            }
        }

        jarList.clear()
    }
}