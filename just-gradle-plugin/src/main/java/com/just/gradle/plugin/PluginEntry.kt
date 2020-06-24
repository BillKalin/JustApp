package com.just.gradle.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.base.app.spi.VariantProcessor
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

public class PluginEntry : Plugin<Project> {
    override fun apply(project: Project) {
        when {
            project.plugins.hasPlugin("com.android.application") || project.plugins.hasPlugin("com.android.dynamic-feature") -> {
                println("applicationapplicationapplicationapplication")

                project.extensions.getByType(AppExtension::class.java).let { appExtension ->
                    appExtension.registerTransform(MainTransformer(project))
                    project.afterEvaluate {
                        appExtension.applicationVariants.forEach { variant ->
                            variantProcessor.forEach { processor ->
                                processor.process(variant)
                            }
                        }
                    }

                }
            }
            project.plugins.hasPlugin("com.android.library") -> {
                println("com.android.librarycom.android.librarycom.android.librarycom.android.library")
                project.extensions.getByType(LibraryExtension::class.java).let { libExtension ->
                    libExtension.registerTransform(MainTransformer(project))
                    project.afterEvaluate {
                        libExtension.libraryVariants.forEach { variant ->
                            variantProcessor.forEach { processor ->
                                processor.process(variant)
                            }
                        }
                    }

                }
            }
        }
    }

    private val variantProcessor: Collection<VariantProcessor> =
        ServiceLoader.load(VariantProcessor::class.java, javaClass.classLoader).toList()
}
