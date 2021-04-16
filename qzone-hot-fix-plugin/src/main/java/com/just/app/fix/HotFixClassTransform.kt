package com.just.app.fix

import com.base.app.asm.ClassTransformer
import com.base.app.spi.transformer.TransformContext
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

@AutoService(ClassTransformer::class)
class HotFixClassTransform : ClassTransformer {

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name == "com/billkalin/justapp/main/SingleTaskActivity") {
               klass.methods.firstOrNull {
                   it.name == "blockPreverify2" && it.desc == "()V"
               }?.instructions?.let {
                   val returnNode = it.toArray().firstOrNull {
                       it.opcode == Opcodes.RETURN
                   }
                   val code = InsnList().apply {
                       add(LdcInsnNode(Type.getType("Lcom/billkalin/justapp/patch/PatchBlock;")))
                       add(MethodInsnNode(Opcodes.INVOKEVIRTUAL,  "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false))
                       add(VarInsnNode(Opcodes.ASTORE, 1))
                       add(InsnNode(Opcodes.ICONST_0))
                       add(VarInsnNode(Opcodes.ISTORE, 2))
                       add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
                       add(VarInsnNode(Opcodes.ALOAD, 1))
                       add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false))

                   }
                   it.insertBefore(returnNode, code/*LdcInsnNode(Type.getType("Lcom/billkalin/justapp/patch/PatchBlock;"))*/)
               }
        }
        return super.transform(context, klass)
    }

}