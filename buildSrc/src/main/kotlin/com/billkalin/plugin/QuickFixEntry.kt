package com.billkalin.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin

class QuickFixEntry : Plugin<Project> {
    override fun apply(target: Project) {
        println("${this::class.simpleName} apply()")
//        if (target.plugins.hasPlugin(ApplicationPlugin::class.java)) {
            target.extensions.getByType(AppExtension::class.java).apply {
                registerTransform(QuickFixTransformer(target, this))
            }
//        }
    }
}