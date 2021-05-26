package com.billkalin.plugin

import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.Modifier
import javassist.expr.Cast
import javassist.expr.MethodCall

object QuickFixUtils {

    fun paramSignatureString(ctMethod: CtMethod): String {
        var string = StringBuilder()
        if (!ctMethod.parameterTypes.isNullOrEmpty()) {
            ctMethod.parameterTypes.forEachIndexed { index, ctClass ->
                string.append(ctClass.name).append(" var${index}").append(",")
            }
        }
        if (string.isNotEmpty() && string.last() == ',') {
            string = string.deleteCharAt(string.length - 1)
        }
        return string.toString()
    }

    fun paramValueString(ctMethod: CtMethod): String {
        var string = StringBuilder()
        if (!ctMethod.parameterTypes.isNullOrEmpty()) {
            ctMethod.parameterTypes.forEachIndexed { index, _ ->
                string.append(" var${index}").append(",")
            }
        }
        if (string.isNotEmpty() && string.last() == ',') {
            string = string.deleteCharAt(string.length - 1)
        }
        return string.toString()
    }

    fun getMethodModifierString(ctMethod: CtMethod): String {
        if (Modifier.isStatic(ctMethod.modifiers)) {
            return "static"
        }
        return ""
    }

    fun getFieldValueString(ctField: CtField, patchClassName: String, modifyClassName: String): String {
        val isStatic = Modifier.isStatic(ctField.modifiers)
        val string = StringBuilder().append("{")
        if (isStatic) {
            if (Modifier.isPublic(ctField.modifiers)) {
                if (isRfile(ctField.declaringClass.name)) {
                    string.append("\$_").append("=").append(ctField.constantValue).append(";")
                } else {
                    string.append("\$_").append("=").append("\$proceed(\$\$);")
                }
            } else {
                if (ctField.declaringClass.name == patchClassName) {
                    string.append("\$_").append("=").append("(\$r)")
                        .append(Constants.REFLECT_CLASS_NAME).append(".getStaticFieldValue(").append("\"")
                        .append(ctField.name).append("\"").append(",").append("$modifyClassName.class").append(");")
                } else {
                    string.append("\$_").append("=").append("(\$r)")
                        .append(Constants.REFLECT_CLASS_NAME).append(".getStaticFieldValue(")
                        .append("\"").append(ctField.name).append("\"").append(",")
                        .append("${ctField.declaringClass.name}.class").append(");")
                }
            }
        } else {
            string.append("java.lang.Object instance;")
            string.append("if (\$0 instanceOf ${patchClassName}) {")
            string.append("instance = ((${patchClassName})\$0).${Constants.PATCH_ORIGIN_CLASS_FIELD_NAME};")
            string.append("}")
            string.append("else { instance = \$0; }")
            string.append("\$_").append("=").append("(\$r)").append(Constants.REFLECT_CLASS_NAME)
                .append(".getFieldValue(").append("\"${ctField.name}\",").append("instance,")
                .append("${ctField.declaringClass.name}.class);")
        }
        string.append("}");
        QuickFixTransformer.sLogger.quiet("getFieldValueString: string = ${string.toString()}")
        return string.toString()
    }

    fun setFieldValueString(ctField: CtField, patchClassName: String, modifyClassName: String): String {
        val string = StringBuilder().append("{")
        val isStatic = Modifier.isStatic(ctField.modifiers)
        if (isStatic) {
            if (Modifier.isPublic(ctField.modifiers)) {
                string.append("\$_").append("=").append("\$proceed(\$\$);")
            } else {
                if (ctField.declaringClass.name == patchClassName) {
                    string.append("\$_").append("=").append("(\$r)")
                        .append(Constants.REFLECT_CLASS_NAME).append(".setStaticFieldValue(")
                        .append("\"").append(ctField.name).append("\"").append(",").append("${modifyClassName}.class);")
                } else {
                    string.append("\$_").append("=").append("(\$r)")
                        .append(Constants.REFLECT_CLASS_NAME).append(".setStaticFieldValue(")
                        .append("\"").append(ctField.name).append("\"").append(",")
                        .append("${ctField.declaringClass.name}.class);")
                }
            }
        } else {
            string.append("java.lang.Object instance;")
            string.append("if(\$0 instanceOf ${patchClassName}) {")
            string.append("instance").append("=")
                .append("(($patchClassName)\$0).${Constants.PATCH_ORIGIN_CLASS_FIELD_NAME};")
            string.append("}")
            string.append("else {")
            string.append("instance").append("=").append("\$0;")
            string.append("}")
            string.append("\$_").append("=").append("(\$r)").append(Constants.REFLECT_CLASS_NAME)
                .append(".setFieldValue(")
            string.append("\"").append(ctField.name).append("\"").append(",").append("\$1,").append("instance,")
                .append("${ctField.declaringClass.name}.class);")
        }
        string.append("}")
        QuickFixTransformer.sLogger.quiet("setFieldValueString: string = ${string.toString()}")
        return string.toString()
    }

    fun getCastString(cast: Cast, ctClass: CtClass): String {
        val string = StringBuilder().append("{")
        string.append("if(\$1 == this) {")
        string.append("\$_").append("=").append("((${ctClass.name})\$1).")
            .append(Constants.PATCH_ORIGIN_CLASS_FIELD_NAME).append(";")
        string.append("}")
        string.append("else {")
        string.append("\$_").append("=").append("(\$r)\$1;")
        string.append("}")
        string.append("}")
        QuickFixTransformer.sLogger.quiet("getCastString: string = ${string.toString()}")
        return string.toString()
    }

    fun isRfile(name: String): Boolean {
        if (name.lastIndexOf('R') < 0)
            return false
        return Constants.R_File.contains(name.substring(name.indexOf('R')))
    }

    fun getInnerClassString(
        signature: String,
        newClass: String,
        isStatic: Boolean,
        methodClass: String
    ): String {
        val string = StringBuilder().append("{")
        val paramString = getParamsSignatureString(signature, newClass)
        if (isStatic) {
            if (paramString.isEmpty()) {
                string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME)
                    .append(".reflectConstruct(").append(methodClass).append(",")
                string.append("\$args,").append("new Class[]{").append(paramString).append("});")
            } else {
                string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME)
                    .append(".reflectConstruct(").append(methodClass).append(",")
                string.append("\$args,").append("null});")
            }
        } else {
            if (paramString.isEmpty()) {
                string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME)
                    .append(".reflectConstruct(").append(methodClass).append(",")
                string.append("java.lang.Object[] params= ")
                    .append(Constants.PATCH_GET_REAL_METHOD_NAME).append("(\$args);")
                string.append("\$args,").append("new Class[]{").append(paramString).append("});")
            } else {
                string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME)
                    .append(".reflectConstruct(").append(methodClass).append(",")
                string.append("\$args,").append("null});")
            }
        }
        string.append("}")
        QuickFixTransformer.sLogger.quiet("getInnerClassString: string = ${string.toString()}")
        return string.toString()
    }

    fun createInnerClassString(
        signature: String,
        newClass: String,
        isStatic: Boolean,
        methodClass: String
    ): String {
        if (signature.isNullOrEmpty()) {
            return "{\$_=(\$r)\$proceed(\$\$);};"
        }
        val string = StringBuilder().append("{")
        val paramString = getParamsSignatureString(signature, newClass)
        if (isStatic) {
            if (paramString.isNotEmpty()) {
                string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".reflectConstruct(")
                    .append("\"").append(methodClass).append("\"").append(",")
                string.append("\$args,").append("new Class[]{").append(paramString).append("});")
            } else {
                string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".reflectConstruct(")
                    .append("\"").append(methodClass).append("\"").append(",")
                string.append("\$args,").append("null);")
            }
        } else {
            if (paramString.isNotEmpty()) {
                string.append("java.lang.Object[] params= ")
                    .append(Constants.PATCH_GET_REAL_METHOD_NAME).append("(\$args);")
                string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".reflectConstruct(").append("\"").append(methodClass).append("\"").append(",")
                string.append("params,").append("new Class[]{").append(paramString).append("});")
            } else {
                string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME)
                    .append(".reflectConstruct(").append("\"").append(methodClass).append("\"").append(",")
                string.append("\$args,").append("null);")
            }
        }
        string.append("}")
        QuickFixTransformer.sLogger.quiet("createInnerClassString: string = ${string.toString()}")
        return string.toString()
    }

    private fun getParamsSignatureString(signature: String, patchClass: String): String {
        if (signature.isNullOrEmpty())
            return ""
        val string = StringBuilder()
        var index = 1
        var isArray = false
        val end = signature.indexOf(')')
        while (index < end) {
            if (signature[index] == Constants.TYPE_OBJECT && signature.indexOf(Constants.TYPE_END_PACKAGE) != -1) {
                val str =
                    signature.substring(index + 1, signature.indexOf(Constants.TYPE_END_PACKAGE)).replace('/', '.')
                if (str == patchClass) {
                    string.append(str)//TODO 这里需要找到对应的modify class name
                } else {
                    string.append(str)
                }
                if (isArray) {
                    isArray = false
                    string.append("[]")
                }
                index = signature.indexOf(Constants.TYPE_END_PACKAGE, index)
                string.append(".class,")
            }

            if (Constants.TYPE_PRIMATE.contains(signature[index])) {
                when (signature[index]) {
                    'Z' -> {
                        string.append("boolean")
                    }
                    'C' -> {
                        string.append("char")
                    }
                    'B' -> {
                        string.append("byte")
                    }
                    'S' -> {
                        string.append("short")
                    }
                    'I' -> {
                        string.append("int")
                    }
                    'J' -> {
                        string.append("long")
                    }
                    'F' -> {
                        string.append("float")
                    }
                    'D' -> {
                        string.append("double")
                    }
                }

                if (isArray) {
                    isArray = false
                    string.append("[]")
                }
                string.append(".class")
            }

            if (signature[index] == Constants.TYPE_ARRAY) {
                isArray = true
            }

            index++
        }

        if (string.isNotEmpty() && string.last() == ',') {
            string.deleteCharAt(string.length - 1)
        }
        return string.toString()
    }

    fun getMethodCallString(methodCall: MethodCall, isInStaticMethod: Boolean, newClass: CtClass): String {
        val string = StringBuilder().append("{")
        val paramClassString = getParamClassParamsString(methodCall.method.parameterTypes)
        string.append(methodCall.method.declaringClass.name).append(" instance;")
        val callIsStatic = Modifier.isStatic(methodCall.method.modifiers)
        if (callIsStatic) {//静态方法调用
            if (isInStaticMethod) {//在静态方法里调用
                if (Modifier.isPublic(methodCall.method.modifiers)) {
                    string.append("\$_=").append("\$proceed(\$\$);")
                } else {
                    if (paramClassString.isEmpty()) {
                        //ReflectUtils.invokeStaticMethod(methodName, class, instance, null, args)
                        string.append("\$_=").append("(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".invokeStaticMethod(").append("\"").append(getMethodNameByMethodParamSignatureWithReturnType(
                            getMethodParamSignatureWithReturnType(methodCall.method))).append("\"").append(",").append(methodCall.method.declaringClass.name).append(".class,").append("null,").append("\$args").append(");")
                    } else {
                        //ReflectUtils.invokeStaticMethod(methodName, class, instance, new Class[]{xx.class, xxx.class}, args)
                        string.append("\$_=").append("(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".invokeStaticMethod(").append("\"").append(getMethodNameByMethodParamSignatureWithReturnType(
                            getMethodParamSignatureWithReturnType(methodCall.method))).append("\"").append(",").append(methodCall.method.declaringClass.name).append(".class,").append("new Class[]{").append(paramClassString).append("},").append("\$args").append(");")
                    }
                }
            } else {
                //Object[] params = getRealParams(args);
                string.append("java.lang.Object[] params=").append(Constants.PATCH_GET_REAL_METHOD_NAME).append("(\$args);")
                if (paramClassString.isEmpty()) {
                    string.append("\$_=").append("(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".invokeStaticMethod(").append("\"").append(getMethodNameByMethodParamSignatureWithReturnType(
                        getMethodParamSignatureWithReturnType(methodCall.method))).append("\"").append(",").append(methodCall.method.declaringClass.name).append(".class,").append("null,").append("params").append(");")
                } else {
                    string.append("\$_=").append("(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".invokeStaticMethod(").append("\"").append(getMethodNameByMethodParamSignatureWithReturnType(
                        getMethodParamSignatureWithReturnType(methodCall.method))).append("\"").append(",").append(methodCall.method.declaringClass.name).append(".class,").append("new Class[]{").append(paramClassString).append("},").append("params").append(");")
                }
            }
        } else {
            if (isInStaticMethod) {//在静态方法里调用
                //instance = ((methodCallClass)arg0);
                string.append("instance=").append("(").append(methodCall.method.declaringClass.name).append(")\$0;")
                //ReflectUtils.invokeMethod(methodName, class, instance, null, args)
                if (paramClassString.isEmpty()) {
                    string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".invokeMethod(").append("\"").append((getMethodNameByMethodParamSignatureWithReturnType(
                        getMethodParamSignatureWithReturnType(methodCall.method)))).append("\"").append(",").append(methodCall.method.declaringClass.name).append(".class,").append("instance,").append("null,").append("\$args").append(");")
                } else {
                    //ReflectUtils.invokeMethod(methodName, class, instance, new Class[]{xx.class, ccc.class}, args)
                    string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".invokeMethod(").append("\"").append((getMethodNameByMethodParamSignatureWithReturnType(
                        getMethodParamSignatureWithReturnType(methodCall.method)))).append("\"").append(",").append(methodCall.method.declaringClass.name).append(".class,").append("instance,").append("new Class[]{$paramClassString},").append("\$args);")
                }
            } else {
                /***
                 * if (arg0 == this) {
                 * instance = ((newClass)this).origin;
                 * } else {
                 * instance = arg0;
                 * }
                 *
                 */
                string.append("if(\$0 == this) {")
                string.append("instance = ((${newClass.name})\$0).").append(Constants.PATCH_ORIGIN_CLASS_FIELD_NAME).append(";")
                string.append("} else {")
                string.append("instance = ").append("\$0;")
                string.append("}")
                //ReflectUtils.invokeMethod(methodName, class, instance, null, args)
                if (paramClassString.isEmpty()) {
                    string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".invokeMethod(").append("\"").append((getMethodNameByMethodParamSignatureWithReturnType(
                        getMethodParamSignatureWithReturnType(methodCall.method)))).append("\"").append(",").append(methodCall.method.declaringClass.name).append(".class,").append("instance,").append("null,").append("\$args").append(");")
                } else {
                    //ReflectUtils.invokeMethod(methodName, class, instance, new Class[]{xx.class, ccc.class}, args)
                    string.append("\$_=(\$r)").append(Constants.REFLECT_CLASS_NAME).append(".invokeMethod(").append("\"").append((getMethodNameByMethodParamSignatureWithReturnType(
                        getMethodParamSignatureWithReturnType(methodCall.method)))).append("\"").append(",").append(methodCall.method.declaringClass.name).append(".class,").append("instance,").append("new Class[]{$paramClassString},").append("\$args);")
                }
            }
        }
        string.append("}")
        QuickFixTransformer.sLogger.quiet("getMethodCallString: string = ${string.toString()}")
        return string.toString()
    }

    //返回:
    private fun getParamClassParamsString(array: Array<CtClass>): String {
        val string = StringBuilder()
        array.forEach {
            string.append(it.name).append(".class,")
        }
        if (string.isNotEmpty() && string.last() == ',') {
            string.deleteCharAt(string.length - 1)
        }
        return string.toString()
    }

    //返回：String getMethodParamSignatureWithReturnType(javassist.CtMethod,xx,xx)
    private fun getMethodParamSignatureWithReturnType(ctMethod: CtMethod): String {
        val string = StringBuilder()
        string.append(ctMethod.returnType.name).append(" ")
        string.append(ctMethod.name).append("(")
        ctMethod.parameterTypes.forEach {
            string.append(it.name)
            string.append(",")
        }
        if (string.isNotEmpty() && string.last() == ',') {
            string.deleteCharAt(string.length - 1)
        }
        string.append(")")
        return string.toString()
    }

    private fun getMethodNameByMethodParamSignatureWithReturnType(methodParamSignatureWithReturnType: String): String {
        val string = methodParamSignatureWithReturnType.substring(
            methodParamSignatureWithReturnType.indexOf(' ') + 1,
            methodParamSignatureWithReturnType.indexOf('(')
        )
        return string
    }

    fun getInvokeSuperString(m: CtMethod, originClass: String): String {
        val string = StringBuilder().append("{")
        if (m.returnType != CtClass.voidType) {
            string.append("\$_=(\$r)")
        }
        if (m.parameterTypes.isNullOrEmpty()) {
            if (originClass.isEmpty()) {
                string.append(Constants.PATCH_STATIC_SUPER_METHOD_PREFIX + m.name).append("(this,").append(Constants.PATCH_ORIGIN_CLASS_FIELD_NAME).append(");")
            } else {
                string.append(Constants.PATCH_STATIC_SUPER_METHOD_PREFIX + m.name).append("(null,").append(originClass).append(");")
            }
        } else {
            if (originClass.isEmpty()) {
                string.append(Constants.PATCH_STATIC_SUPER_METHOD_PREFIX + m.name).append("(this,").append(Constants.PATCH_ORIGIN_CLASS_FIELD_NAME).append(",\$\$").append(");")
            } else {
                string.append(Constants.PATCH_STATIC_SUPER_METHOD_PREFIX + m.name).append("(null,").append(originClass).append(",\$\$").append(");")
            }
        }
        string.append("}")
        QuickFixTransformer.sLogger.quiet("getInvokeSuperString: string = ${string.toString()}")
        return string.toString()
    }
}