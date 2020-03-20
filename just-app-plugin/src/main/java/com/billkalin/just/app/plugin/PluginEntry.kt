package com.billkalin.just.app.plugin

import com.android.build.gradle.AppExtension
import com.billkalin.just.app.plugin.transformers.RTransformer
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginEntry : Plugin<Project> {
    override fun apply(project: Project) {
        val android = project.extensions.getByType(AppExtension::class.java)
        android.registerTransform(RTransformer(project))
    }
}