package com.just.gradle.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.base.app.spi.transformer.Transformer
import org.gradle.api.Project
import java.util.*

class MainTransformer(private val project: Project) : Transform() {

    internal val transformers =
        ServiceLoader.load(Transformer::class.java, javaClass.classLoader).toList()

    override fun getName(): String = "just-app"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    override fun isIncremental(): Boolean = true
    override fun isCacheable(): Boolean = true

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = when {
        project.plugins.hasPlugin("com.android.application") -> TransformManager.SCOPE_FULL_PROJECT
        project.plugins.hasPlugin("com.android.library") -> TransformManager.PROJECT_ONLY
        project.plugins.hasPlugin("com.android.dynamic-feature") -> TransformManager.SCOPE_FEATURES
        else -> {
            TODO("")
        }
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
        return when {
            project.plugins.hasPlugin("com.android.application") -> TransformManager.SCOPE_FULL_PROJECT
            project.plugins.hasPlugin("com.android.library") -> TransformManager.PROJECT_ONLY
            project.plugins.hasPlugin("com.android.dynamic-feature") -> TransformManager.SCOPE_FEATURES
            else -> {
                super.getReferencedScopes()
            }
        }
    }

    override fun transform(transformInvocation: TransformInvocation) {
        MainTransformInvocation(transformInvocation, this).apply {
            if (isIncremental) {
                doIncrementalTransform()
            } else {
                outputProvider.deleteAll()
                doFullTransform()
            }
        }
    }
}