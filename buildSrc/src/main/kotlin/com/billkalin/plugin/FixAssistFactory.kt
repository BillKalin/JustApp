package com.billkalin.plugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.ClassFile

object FixAssistFactory {

    fun createAssistClass(
        classPool: ClassPool,
        modifyClass: CtClass,
        patchClass: CtClass,
        invokeSuperMethod: CtMethod): CtClass {
        var assistClass = classPool.getOrNull(patchClass.name + Constants.PATCH_ASSIST_CLASS_SUFFIX)
        if (assistClass == null) {
            assistClass = classPool.makeClass(patchClass.name + Constants.PATCH_ASSIST_CLASS_SUFFIX).apply {
                classFile.majorVersion = ClassFile.JAVA_7
                if (modifyClass.superclass != null) {
                    superclass = modifyClass.superclass
                }
            }
        }
        if (assistClass.isFrozen) {
            assistClass.defrost()
        }
        val methodBody = StringBuilder()
        if (invokeSuperMethod.parameterTypes.isNullOrEmpty()) {
            methodBody.append("public static ${invokeSuperMethod.returnType.name} ${Constants.PATCH_SUPER_METHOD_PREFIX}${invokeSuperMethod.name}(")
            methodBody.append("${modifyClass.name} modifyInstance, ${patchClass.name} patchInstance)").append("{")
        } else {
            methodBody.append("public static ${invokeSuperMethod.returnType.name} ${Constants.PATCH_SUPER_METHOD_PREFIX}${invokeSuperMethod.name}(")
            methodBody.append("${modifyClass.name} modifyInstance, ${patchClass.name} patchInstance)").append(",")
            val params = QuickFixUtils.paramSignatureString(invokeSuperMethod)
            methodBody.append(params).append(")").append("{")
        }

        methodBody.append("return patchInstance.${invokeSuperMethod.name}(")
        val values = QuickFixUtils.paramValueString(invokeSuperMethod)
        methodBody.append(values).append(");")
        methodBody.append(values).append("}")
        val method = CtMethod.make(methodBody.toString(), assistClass)
        assistClass.addMethod(method)

        return assistClass
    }

}