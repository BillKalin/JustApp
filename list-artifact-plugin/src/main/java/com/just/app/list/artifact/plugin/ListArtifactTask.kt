package com.just.app.list.artifact.plugin

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.artifact.BuildArtifactType
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.scope.AnchorOutputType
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.base.app.spi.util.file
import com.base.app.spi.util.project
import com.base.app.spi.util.scope
import com.base.app.spi.util.touch
import com.just.app.plugin.list.artifact.plugin.Build
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.AbstractMap

open class ListArtifactTask : DefaultTask() {

    lateinit var variant: BaseVariant

    @TaskAction
    fun execute() {
        val artifactTypes = arrayOf(
            AnchorOutputType::class, InternalArtifactType::class,
            BuildArtifactType::class
        ).map {
            it.sealedSubclasses
        }.flatten().map {
            it.objectInstance as ArtifactType<out FileSystemLocation>
        }.map {
            it.javaClass.simpleName to it
        }.toMap()

        val logger =
            File(variant.project.buildDir, "reports").file(Build.ARTIFACT).file(variant.name)
                .file("reports.txt").touch().printWriter()

        artifactTypes.entries.map { it ->
            var values: List<File>? = null
            try {
                values =
                    variant.scope.artifacts.getFinalProductAsFileCollection(it.value).orNull?.toList()
            } catch (e: Exception) {
                logger.println("key ====>  ${it.value.name()}")
                logger.println(e.message)
            }
            AbstractMap.SimpleEntry<String, Collection<File>>(
                it.key,
                values
            )
        }.forEach {
            logger.println("key = ${it.key}, files = ${it.value}")
        }
        logger.close()
    }

}