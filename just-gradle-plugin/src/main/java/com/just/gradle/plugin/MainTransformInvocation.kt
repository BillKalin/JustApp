package com.just.gradle.plugin

import com.android.build.api.transform.*
import com.base.app.spi.transformer.ArtifactManager
import com.base.app.spi.transformer.TransformContext
import com.base.app.spi.util.*
import java.io.File
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.Future

class MainTransformInvocation(
    internal val delegate: TransformInvocation,
    internal val transform: MainTransformer
) : TransformInvocation, TransformContext, ArtifactManager {

    override fun getInputs(): MutableCollection<TransformInput> {
        return delegate.inputs
    }

    override fun getSecondaryInputs(): MutableCollection<SecondaryInput> = delegate.secondaryInputs

    override fun getReferencedInputs(): MutableCollection<TransformInput> =
        delegate.referencedInputs

    override fun isIncremental(): Boolean = delegate.isIncremental

    override fun getOutputProvider(): TransformOutputProvider = delegate.outputProvider

    override fun getContext(): Context = delegate.context

    internal fun doFullTransform() = doTransformer(this::doTransformerFully)

    internal fun doIncrementalTransform() = doTransformer(this::transformIncrementally)

    private fun doPreTransformer() {
        transform.transformers.forEach {
            it.onPreTransform(this)
        }
    }

    private fun doPostTransformer() {
        tasks.forEach {
            it.get()
        }
        transform.transformers.forEach {
            it.onPostTransform(this)
        }
    }

    private fun doTransformer(block: () -> Unit) {
        doPreTransformer()
        block()
        doPostTransformer()
    }

    private fun doTransformerFully() {
        this.inputs.map {
            it.jarInputs + it.directoryInputs
        }.flatten().forEach { input ->
            tasks += executor.submit {
                val format = if (input is DirectoryInput) Format.DIRECTORY else Format.JAR
                input.transform(
                    outputProvider.getContentLocation(
                        input.name,
                        input.contentTypes,
                        input.scopes,
                        format
                    ), this
                )
            }
        }
    }

    private fun transformIncrementally() {
        this.inputs.parallelStream().forEach {
            it.directoryInputs.parallelStream().filter { it.changedFiles.isNotEmpty() }
                .forEach { dir ->
                    val base = dir.file.toURI()
                    tasks += executor.submit {
                        doIncrementalTransform(dir, base)
                    }
                }
            it.jarInputs.parallelStream().filter { it.status != Status.NOTCHANGED }.forEach { jar ->
                tasks += executor.submit {
                    doIncrementalTransform(jar)
                }
            }
        }
    }

    private fun doIncrementalTransform(jarInput: JarInput) {
        when (jarInput.status) {
            Status.REMOVED -> jarInput.file.delete()
            Status.ADDED, Status.CHANGED -> {
                outputProvider.let { provider ->
                    jarInput.transform(
                        provider.getContentLocation(
                            jarInput.name,
                            jarInput.contentTypes,
                            jarInput.scopes,
                            Format.JAR
                        ), this
                    )
                }
            }
            else -> {
            }
        }
    }

    private fun doIncrementalTransform(dirInput: DirectoryInput, base: URI) {
        dirInput.changedFiles.forEach { (file, status) ->
            when (status) {
                Status.REMOVED -> {
                    outputProvider.let { provider ->
                        provider.getContentLocation(
                                dirInput.name,
                                dirInput.contentTypes,
                                dirInput.scopes,
                                Format.DIRECTORY
                            ).parentFile?.listFiles()?.asSequence()?.filter { it.isDirectory }
                            ?.map { File(it, dirInput.file.toURI().relativize(file.toURI()).path) }
                            ?.filter { it.exists() }?.forEach {
                                it.delete()
                            }
                    }
                    file.delete()
                }
                Status.ADDED, Status.CHANGED -> {
                    val root = outputProvider.getContentLocation(
                        dirInput.name,
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY
                    )
                    val output = File(root, base.relativize(file.toURI()).path)
                    file.transform(output) { bytes ->
                        bytes.transform(this)
                    }
                }
                else -> {

                }
            }
        }
    }

    private val tasks = mutableListOf<Future<*>>()
    private val executor = Executors.newWorkStealingPool(NCPU)

    override val name: String
        get() = delegate.context.variantName
    override val projectDir: File
        get() = delegate.project.projectDir
    override val buildDir: File
        get() = delegate.project.buildDir
    override val temporaryDir: File
        get() = delegate.context.temporaryDir
    override val reportsDir: File
        get() = File(buildDir, "reports").also { it.mkdirs() }
    override val bootClasspath: Collection<File>
        get() = delegate.bootClasspath
    override val compileClasspath: Collection<File>
        get() = delegate.compileClasspath
    override val runtimeClasspath: Collection<File>
        get() = delegate.runtimeClasspath
    override val artifacts: ArtifactManager
        get() = variant.artifacts

    //    override val klassPool: KlassPool
//        get() = TODO("Not yet implemented")
    override val applicationId: String
        get() = delegate.applicationId
    override val originalApplicationId: String
        get() = delegate.originalApplicationId
    override val isDebuggable: Boolean
        get() = delegate.isDebuggable
    override val isDataBindingEnabled: Boolean
        get() = delegate.isDataBindingEnabled

    override fun get(type: String): Collection<File> {
        return artifacts.get(type)
    }

    override fun hasProperty(name: String): Boolean = project.hasProperty(name)

    private fun QualifiedContent.transform(
        output: File,
        transformInvocation: MainTransformInvocation
    ) {
        file.transform(output) { byteArray ->
            byteArray.transform(transformInvocation)
        }
    }

    private fun ByteArray.transform(transformInvocation: MainTransformInvocation): ByteArray {
        return transformInvocation.transform.transformers.fold(this) { bytes, transformer ->
            transformer.transform(this@MainTransformInvocation, bytes)
        }
    }
}