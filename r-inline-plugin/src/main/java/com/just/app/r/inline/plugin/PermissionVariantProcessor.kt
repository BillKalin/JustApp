package com.just.app.r.inline.plugin

import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.VariantTypeImpl
import com.base.app.spi.VariantProcessor
import com.base.app.spi.util.project
import com.base.app.spi.util.variantData
import com.google.auto.service.AutoService

@AutoService(VariantProcessor::class)
class PermissionVariantProcessor : VariantProcessor {

    companion object {
        private const val TASK_NAME = "PermissionChecker"
    }

    override fun process(variant: BaseVariant) {
        val variantType = variant.variantData.type as VariantTypeImpl
        println("PermissionVariantProcessor: variantType = $variantType")
        if (variantType == VariantTypeImpl.FEATURE)
            return

        val tasks = variant.project.tasks
//        val permissionTask = tasks.findByName(TASK_NAME) ?: tasks.create(TASK_NAME)
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