package com.base.app.asm

import com.base.app.spi.transformer.TransformContext
import com.base.app.spi.transformer.Transformer
import com.google.auto.service.AutoService
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.util.*

@AutoService(Transformer::class)
public class AsmTransform : Transformer {

    internal val transformers: Collection<ClassTransformer>

    constructor() : this(
        *ServiceLoader.load(
            ClassTransformer::class.java,
            AsmTransform::class.java.classLoader
        ).toList().toTypedArray()
    )

    constructor(vararg transform: ClassTransformer) {
        this.transformers = transform.asList()
    }

    override fun onPreTransform(context: TransformContext) {
        this.transformers.forEach {
            it.onPreTransform(context)
        }
    }

    override fun transform(context: TransformContext, bytecode: ByteArray): ByteArray {
        return ClassWriter(ClassWriter.COMPUTE_MAXS).also { writer ->
            this.transformers.fold(ClassNode().also { kclass ->
                ClassReader(bytecode).accept(kclass, ClassReader.EXPAND_FRAMES)
            }) { kclass, transformer ->
                transformer.transform(context, kclass)
            }.accept(writer)
        }.toByteArray()
    }

    override fun onPostTransform(context: TransformContext) {
        this.transformers.forEach {
            it.onPostTransform(context)
        }
    }
}
