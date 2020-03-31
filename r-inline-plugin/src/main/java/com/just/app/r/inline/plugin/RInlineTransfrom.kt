package com.just.app.r.inline.plugin

import com.base.app.asm.ClassTransformer
import com.base.app.spi.transformer.TransformContext
import com.base.app.spi.util.file
import com.base.app.spi.util.touch
import com.google.auto.service.AutoService
import com.just.app.plugin.r.inline.plugin.Build
import org.objectweb.asm.tree.ClassNode
import java.io.PrintWriter

@AutoService(ClassTransformer::class)
public class RInlineTransform : ClassTransformer {

    companion object {
        var isInit = false
    }

    private lateinit var appPkg: String
    private lateinit var logger: PrintWriter

    override fun onPreTransform(context: TransformContext) {
        super.onPreTransform(context)
        this.appPkg = context.originalApplicationId.replace("/", ".")
        this.logger =
            context.reportsDir.file(Build.ARTIFACT).file(context.name).file("report.txt").touch()
                .printWriter()
        println("RInlineTransform:: onPreTransform -> appPkg= $appPkg")
        this.logger.println("RInlineTransform:: onPreTransform -> appPkg= $appPkg")
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (!isInit) {
            logger.println("RInlineTransform:: applicationId = ${context.applicationId}, buildDir = ${context.buildDir.absolutePath}")
            isInit = true
        }
        return super.transform(context, klass)
    }

    override fun onPostTransform(context: TransformContext) {
        super.onPostTransform(context)
        this.logger.println("onPostTransform() onPostTransform() onPostTransform()")
        println("onPostTransform() onPostTransform() onPostTransform()")
        this.logger.close()
    }
}
