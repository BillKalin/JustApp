package com.billkalin.android.hot.quickfix.base.plugin

import com.android.dx.rop.code.AccessFlags
import com.base.app.asm.ClassTransformer
import com.base.app.spi.transformer.TransformContext
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.lang.reflect.Modifier

@AutoService(ClassTransformer::class)
class QuickFixClassInsert : ClassTransformer {

    companion object {
        const val FIX_CLASS = "com.billkalin.android.hot.quickhotfix.QuickHotFix"
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        insertCode(klass)
        return super.transform(context, klass)
    }

    private fun insertCode(classNode: ClassNode) {
        if (Modifier.isAbstract(classNode.access) || Modifier.isInterface(classNode.access))
            return
        if (classNode.methods.size <= 1)
            return
        if (!classNode.name.replace('/', '.').startsWith("com.billkalin.justapp")) {
            return
        }
        if (classNode.name.substringAfterLast('/').contains("JustApp"))
            return
        //新增字段静态字段mChange
        val fixClassDesc = "L" + FIX_CLASS.replace('.', '/') + ";"
        val staticField = FieldNode(
            Opcodes.ACC_PUBLIC.or(Opcodes.ACC_STATIC),
            "mChange",
            fixClassDesc,
            null,
            null
        )
        classNode.fields.add(staticField)
        //插入逻辑代码
        //if(mChange != null) {
        //  return mChange.dispatch("", new Object[]{...})
        //}
        //
        classNode.methods.asSequence().filter {
            !(it.name == "<init>" || it.name == "<cinit>" || Modifier.isNative(it.access)
                    || Modifier.isAbstract(it.access)/* || Modifier.isStatic(it.access)*/
                    || (it.access.and(AccessFlags.ACC_SYNTHETIC) != 0 && it.access.and(Modifier.PRIVATE) == 0))
        }.forEach { method ->
            val className = classNode.name
            val argumentsType = Type.getArgumentTypes(method.desc)
            val isStatic = Modifier.isStatic(method.access)

            val returnType = Type.getReturnType(method.desc)
            val inst = InsnList().apply {
                val startLabel = LabelNode()
                add(FieldInsnNode(Opcodes.GETSTATIC, className, "mChange", fixClassDesc))
                add(JumpInsnNode(Opcodes.IFNULL, startLabel))
                add(FieldInsnNode(Opcodes.GETSTATIC, className, "mChange", fixClassDesc))
                add(LdcInsnNode(method.name))
                addIndex(argumentsType.size)
                add(TypeInsnNode(Opcodes.ANEWARRAY, Type.getInternalName(Any::class.java)))
                var loadIndex = if (isStatic) 0 else 1
                argumentsType.forEachIndexed { index, type ->
                    add(InsnNode(Opcodes.DUP))
                    addIndex(index)
                    if (index >= 1) {
                        if (argumentsType[index - 1].descriptor == "J" || argumentsType[index - 1].descriptor == "D") {
                            loadIndex++
                        }
                    }
                    if (!createPrimateObject(this, loadIndex, type.descriptor)) {
                        add(VarInsnNode(Opcodes.ALOAD, loadIndex))
                        add(InsnNode(Opcodes.AASTORE))
                    }
                    loadIndex++
                }
                add(
                    MethodInsnNode(
                        Opcodes.INVOKEINTERFACE,
                        FIX_CLASS.replace('.', '/'),
                        "dispatch",
                        "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
                        true
                    )
                )
                castPrimateToObj(this, returnType.descriptor)
                add(InsnNode(getReturnNode(returnType.descriptor)))
                add(startLabel)
            }
            method.instructions.insert(inst)
        }
    }

    private fun InsnList.addIndex(index: Int) {
        val insnNode = if (index < 5) {
            InsnNode(Opcodes.ICONST_0 + index)
        } else {
            IntInsnNode(Opcodes.BIPUSH, index)
        }
        add(insnNode)
    }

    private fun getReturnNode(desc: String): Int = when (desc) {
        "V" -> {
            Opcodes.RETURN
        }
        "Z", "B", "C", "S", "I" -> {
            Opcodes.IRETURN
        }
        "F" -> {
            Opcodes.FRETURN
        }
        "D" -> {
            Opcodes.DRETURN
        }
        "J" -> {
            Opcodes.LRETURN
        }
        else -> {
            Opcodes.ARETURN
        }
    }

    private fun castPrimateToObj(insnList: InsnList, desc: String) {
        when (desc) {
            "Z" -> {
                val booleanCls = Type.getInternalName(Boolean::class.java)
                insnList.add(TypeInsnNode(Opcodes.CHECKCAST, booleanCls))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        booleanCls,
                        "booleanValue",
                        "()Z",
                        false
                    )
                )
            }
            "B" -> {
                val booleanCls = Type.getInternalName(Byte::class.java)
                insnList.add(TypeInsnNode(Opcodes.CHECKCAST, booleanCls))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        booleanCls,
                        "byteValue",
                        "()B",
                        false
                    )
                )
            }
            "C" -> {
                val booleanCls = Type.getInternalName(Character::class.java)
                insnList.add(TypeInsnNode(Opcodes.CHECKCAST, booleanCls))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        booleanCls,
                        "charValue",
                        "()C",
                        false
                    )
                )
            }
            "S" -> {
                val booleanCls = Type.getInternalName(Short::class.java)
                insnList.add(TypeInsnNode(Opcodes.CHECKCAST, booleanCls))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        booleanCls,
                        "shortValue",
                        "()S",
                        false
                    )
                )
            }
            "I" -> {
                val booleanCls = Type.getInternalName(Int::class.java)
                insnList.add(TypeInsnNode(Opcodes.CHECKCAST, booleanCls))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        booleanCls,
                        "intValue",
                        "()I",
                        false
                    )
                )
            }
            "F" -> {
                val booleanCls = Type.getInternalName(Float::class.java)
                insnList.add(TypeInsnNode(Opcodes.CHECKCAST, booleanCls))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        booleanCls,
                        "floatValue",
                        "()F",
                        false
                    )
                )
            }
            "D" -> {
                val booleanCls = Type.getInternalName(Double::class.java)
                insnList.add(TypeInsnNode(Opcodes.CHECKCAST, booleanCls))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        booleanCls,
                        "doubleValue",
                        "()D",
                        false
                    )
                )
            }
            "J" -> {
                val booleanCls = Type.getInternalName(Long::class.java)
                insnList.add(TypeInsnNode(Opcodes.CHECKCAST, booleanCls))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        booleanCls,
                        "longValue",
                        "()J",
                        false
                    )
                )
            }
            "V" -> {

            }
            else -> {
                val booleanCls = if (desc.startsWith('[')) {
                    desc.substring(0)
                } else {
                    desc.substring(1, desc.length - 1)
                }
                insnList.add(TypeInsnNode(Opcodes.CHECKCAST, booleanCls))
            }
        }
    }


    private fun createPrimateObject(insnList: InsnList, index: Int, desc: String): Boolean {
        return when (desc) {
            "Z" -> {
                val owner = Type.getInternalName(Boolean::class.java)
                insnList.add(VarInsnNode(Opcodes.ILOAD, index))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        owner,
                        "valueOf",
                        "(Z)Ljava/lang/Boolean;"
                    )
                )
                insnList.add(InsnNode(Opcodes.AASTORE))
                true
            }
            "B" -> {
                val owner = Type.getInternalName(Byte::class.java)
                insnList.add(VarInsnNode(Opcodes.ILOAD, index))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        owner,
                        "valueOf",
                        "(B)Ljava/lang/Byte;"
                    )
                )
                insnList.add(InsnNode(Opcodes.AASTORE))
                true
            }
            "C" -> {
                val owner = Type.getInternalName(Character::class.java)
                insnList.add(VarInsnNode(Opcodes.ILOAD, index))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        owner,
                        "valueOf",
                        "(C)Ljava/lang/Character;"
                    )
                )
                insnList.add(InsnNode(Opcodes.AASTORE))
                true
            }
            "S" -> {
                val owner = Type.getInternalName(Short::class.java)
                insnList.add(VarInsnNode(Opcodes.ILOAD, index))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        owner,
                        "valueOf",
                        "(S)Ljava/lang/Short;"
                    )
                )
                insnList.add(InsnNode(Opcodes.AASTORE))
                true
            }
            "I" -> {
                val owner = Type.getInternalName(Int::class.java)
                insnList.add(VarInsnNode(Opcodes.ILOAD, index))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        owner,
                        "valueOf",
                        "(I)Ljava/lang/Integer;"
                    )
                )
                insnList.add(InsnNode(Opcodes.AASTORE))
                true
            }
            "F" -> {
                val owner = Type.getInternalName(Float::class.java)
                insnList.add(VarInsnNode(Opcodes.FLOAD, index))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        owner,
                        "valueOf",
                        "(F)Ljava/lang/Float;"
                    )
                )
                insnList.add(InsnNode(Opcodes.AASTORE))
                true
            }
            "D" -> {
                val owner = Type.getInternalName(Double::class.java)
                insnList.add(VarInsnNode(Opcodes.DLOAD, index))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        owner,
                        "valueOf",
                        "(D)Ljava/lang/Double;"
                    )
                )
                insnList.add(InsnNode(Opcodes.AASTORE))
                true
            }
            "J" -> {
                val owner = Type.getInternalName(Long::class.java)
                insnList.add(VarInsnNode(Opcodes.LLOAD, index))
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        owner,
                        "valueOf",
                        "(J)Ljava/lang/Long;"
                    )
                )
                insnList.add(InsnNode(Opcodes.AASTORE))
                true
            }
            else -> {
                false
            }
        }
    }

}