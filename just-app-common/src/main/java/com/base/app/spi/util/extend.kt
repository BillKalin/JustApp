package com.base.app.spi.util

import com.android.build.api.transform.Context
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.variant.BaseVariantData
import org.gradle.api.Project
import java.io.File

val Context.task: TransformTask
    get() = when (this) {
        is TransformTask -> this
        else -> javaClass.getDeclaredField("this$1").apply {
            isAccessible = true
        }.get(this).run {
            javaClass.getDeclaredField("this$0").apply {
                isAccessible = true
            }.get(this) as TransformTask
        }
    }

inline fun <reified T : BaseExtension> Project.getAndroid(): T =
    extensions.getByName("android") as T

val TransformInvocation.variant: BaseVariant
    get() = project.getAndroid<BaseExtension>().let { android ->
        context.variantName.let { variant ->
            when (android) {
                is AppExtension -> when {
                    variant.endsWith("AndroidTest") -> android.testVariants.single { it.name == variant }
                    variant.endsWith("UnitTest") -> android.unitTestVariants.single { it.name == variant }
                    else -> android.applicationVariants.single { it.name == variant }
                }
                is LibraryExtension -> android.libraryVariants.single { it.name == variant }
                else -> TODO("variant not found")
            }
        }
    }

val BaseVariant.variantData: BaseVariantData
    get() = javaClass.getDeclaredMethod("getVariantData").invoke(this) as BaseVariantData

val TransformInvocation.project: Project
    get() = context.task.project

val TransformInvocation.bootClasspath: Collection<File>
    get() = project.getAndroid<BaseExtension>().bootClasspath

val TransformInvocation.compileClasspath: Collection<File>
    get() = listOf(inputs, referencedInputs).flatten().map {
        it.jarInputs + it.directoryInputs
    }.flatten().map {
        it.file
    }

val TransformInvocation.runtimeClasspath: Collection<File>
    get() = bootClasspath + compileClasspath

val TransformInvocation.applicationId: String
    get() = variant.variantData.applicationId

val TransformInvocation.originalApplicationId: String
    get() = variant.variantData.variantConfiguration.originalApplicationId

val TransformInvocation.isDebuggable: Boolean
    get() = variant.buildType.isDebuggable

val TransformInvocation.isDataBindingEnabled: Boolean
    get() = project.getAndroid<BaseExtension>().dataBinding.isEnabled