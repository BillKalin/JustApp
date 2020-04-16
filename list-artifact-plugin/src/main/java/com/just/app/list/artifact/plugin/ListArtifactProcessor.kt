package com.just.app.list.artifact.plugin

import com.android.build.gradle.api.BaseVariant
import com.base.app.spi.VariantProcessor
import com.base.app.spi.util.project
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
public class ListArtifactProcessor : VariantProcessor {
    override fun process(variant: BaseVariant) {
        val tasks = variant.project.tasks
        val listTask = tasks.findByName("listartifact") ?: tasks.create("listartifact")
        tasks.create("list${variant.name.capitalize()}Artifact", ListArtifactTask::class.java) {
            it.variant = variant
            it.outputs.upToDateWhen {
                false
            }
        }.also {
            it.dependsOn(listTask)
        }
    }
}
