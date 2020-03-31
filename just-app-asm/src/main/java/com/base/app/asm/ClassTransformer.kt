package com.base.app.asm

import com.base.app.spi.transformer.TransformContext
import com.base.app.spi.transformer.TransformListener
import org.objectweb.asm.tree.ClassNode

interface ClassTransformer : TransformListener {

    fun transform(context: TransformContext, klass: ClassNode) = klass

}