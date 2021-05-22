package com.billkalin.plugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.ClassFile

object FixAssistFactory {

    fun createAssistClass(
        classPool: ClassPool,
        sourceClass: CtClass,
        fixClass: CtClass,
        fixMethod: CtMethod): CtClass {
        var ctClass = classPool.getOrNull(fixClass.name + Constants.PATCH_ASSIST_CLASS_SUFFIX)
        if (ctClass == null) {
            ctClass = classPool.makeClass(fixClass.name + Constants.PATCH_ASSIST_CLASS_SUFFIX).apply {
                classFile.majorVersion = ClassFile.JAVA_7
                if (sourceClass.superclass != null) {
                    superclass = sourceClass.superclass
                }
            }
        }
        if (ctClass.isFrozen) {
            ctClass.defrost()
        }
        val methodBody = StringBuilder()
        if (fixMethod.parameterTypes.isNullOrEmpty()) {
            methodBody.append("public static ${fixMethod.returnType.name} ${Constants.PATCH_SUPER_METHOD_PREFIX}${fixMethod.name}(")
            methodBody.append("${sourceClass.name} modifyInstance, ${fixClass.name} fixInstance)").append("{")
        } else {
            methodBody.append("public static ${fixMethod.returnType.name} ${Constants.PATCH_SUPER_METHOD_PREFIX}${fixMethod.name}(")
            methodBody.append("${sourceClass.name} modifyInstance, ${fixClass.name} fixInstance)").append(",")
            val paramStr = StringBuilder()
            fixMethod.parameterTypes.forEachIndexed {index, ctClass ->
                paramStr.append(ctClass.name).append(" var$index").append(",")
            }
            val params = paramStr.substring(0, paramStr.length - 1)
            methodBody.append(params).append(")").append("{")
        }

        methodBody.append("return fixInstance.${fixMethod.name}(")
        val paramValueStr = StringBuilder()
        if (!fixMethod.parameterTypes.isNullOrEmpty()) {
            fixMethod.parameterTypes.forEachIndexed {index, _->
                paramValueStr.append("var$index").append(",")
            }
        }
        val values = paramValueStr.substring(0, paramValueStr.length - 1)
        methodBody.append(values).append(");")
        methodBody.append(values).append("}")
        val method = CtMethod.make(methodBody.toString(), ctClass)
        ctClass.addMethod(method)

        return ctClass
    }

}