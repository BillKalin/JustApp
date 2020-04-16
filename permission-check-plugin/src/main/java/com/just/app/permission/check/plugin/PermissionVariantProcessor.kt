package com.just.app.permission.check.plugin

import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.VariantTypeImpl
import com.base.app.spi.VariantProcessor
import com.base.app.spi.util.project
import com.base.app.spi.util.variantData
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
open class PermissionVariantProcessor : VariantProcessor {

    companion object {
        private const val TASK_NAME = "PermissionChecker"
    }

    override fun process(variant: BaseVariant) {
        val variantType = variant.variantData.type as VariantTypeImpl
        if (variantType == VariantTypeImpl.FEATURE)
            return

        val tasks = variant.project.tasks
        if (tasks.findByName(TASK_NAME) == null) {
            tasks.create(TASK_NAME, PermissionTask::class.java)
                .also { task ->
                    task.variant = variant
                    tasks.findByName("process${variant.name.capitalize()}Manifest")?.doLast {
                        task.run()
                    }
                }
        }
    }
}