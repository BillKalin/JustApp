package com.base.app.spi.util

import com.android.build.api.transform.Context
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.scope.AnchorOutputType
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.android.build.gradle.internal.variant.BaseVariantData
import com.base.app.spi.transformer.ArtifactManager
import org.gradle.api.Project
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import java.io.File
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

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

val BaseVariant.scope
    get() = variantData.scope

val BaseVariant.artifacts: ArtifactManager
    get() = object : ArtifactManager {
        override fun get(type: String): Collection<File> {
            return when (type) {
                ArtifactManager.AAR -> {
                    return scope.getArtifactCollection(
                        AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
                        AndroidArtifacts.ArtifactScope.ALL,
                        AndroidArtifacts.ArtifactType.AAR
                    ).artifactFiles.files
                }
                ArtifactManager.ALL_CLASSES -> {
                    scope.artifacts.getFinalProduct(AnchorOutputType.ALL_CLASSES).toList()
                }
                ArtifactManager.APK -> {
                    scope.artifacts.getFinalProduct(InternalArtifactType.APK).toList()
                }
                ArtifactManager.JAR -> {
                    scope.getArtifactCollection(
                        AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
                        AndroidArtifacts.ArtifactScope.ALL,
                        AndroidArtifacts.ArtifactType.JAR
                    ).artifactFiles.files
                }
                ArtifactManager.JAVAC -> {
                    scope.artifacts.getFinalProduct(InternalArtifactType.JAVAC).toList()
                }
                ArtifactManager.MERGED_ASSETS -> {
                    scope.artifacts.getFinalProduct(InternalArtifactType.MERGED_ASSETS).toList()
                }
                ArtifactManager.MERGED_RES -> {
                    scope.artifacts.getFinalProduct(InternalArtifactType.MERGED_RES).toList()
                }
                ArtifactManager.MERGED_MANIFESTS -> {
                    scope.artifacts.getFinalProduct(InternalArtifactType.MERGED_MANIFESTS).toList()
                }
                ArtifactManager.PROCESSED_RES -> {
                    scope.artifacts.getFinalProduct(InternalArtifactType.PROCESSED_RES).toList()
                }
                ArtifactManager.SYMBOL_LIST -> {
                    scope.artifacts.getFinalProduct(InternalArtifactType.RUNTIME_SYMBOL_LIST)
                        .toList()
                }
                ArtifactManager.SYMBOL_LIST_WITH_PACKAGE_NAME -> {
                    scope.artifacts.getFinalProduct(InternalArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME)
                        .toList()
                }
                ArtifactManager.DATA_BINDING_DEPENDENCY_ARTIFACTS -> {
                    scope.artifacts.getFinalProduct(InternalArtifactType.DATA_BINDING_DEPENDENCY_ARTIFACTS)
                        .toList()
                }
                else -> {
                    Collections.emptyList()
                }
            }
        }
    }

fun <T : FileSystemLocation> Provider<T>.toList(): Collection<File> {
    return Stream.of<File>(this.map(FileSystemLocation::getAsFile).orNull)
        .filter(Objects::nonNull)
        .collect(Collectors.toList())
}