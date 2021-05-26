package com.billkalin.plugin

import com.android.SdkConstants
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.*
import javassist.bytecode.AccessFlag
import javassist.bytecode.ClassFile
import javassist.bytecode.LocalVariableAttribute
import javassist.bytecode.MethodInfo
import javassist.expr.*
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File
import java.util.*
import java.util.jar.JarFile
import kotlin.collections.ArrayList


class QuickFixTransformer(private val project: Project, private val android: AppExtension) :
    Transform() {
    companion object {
        lateinit var sLogger: Logger
    }

    init {
        sLogger = project.logger
    }

    override fun getName(): String = "quick-fix"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean = false

    private val classPool = ClassPool()
    private val mLogger: Logger = project.logger
    private val patchPath: String
        get() {
            return File(project.buildDir, Constants.PATCH_PATH).let {
                if (!it.exists()) {
                    it.mkdir()
                }
                it.absolutePath
            }
        }

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        transformInvocation ?: return
        val outputProvider = transformInvocation.outputProvider
        outputProvider.deleteAll()

        android.bootClasspath.forEach {
            classPool.appendClassPath(it.absolutePath)
            mLogger.quiet("bootclass path: ${it.absolutePath}")
        }
        //获取所有的类
        val allClass =
            toCtClasses(transformInputs = transformInvocation.inputs, classPool = classPool)
        //读取修改的类的注解,得到修改的类和方法
        readAnnotation(allClass, mLogger)
        //扫描是否有调用super的类和方法
        scanFixSuperMethod()
        fixMethodList.forEach {
            mLogger.quiet("fixMethod: $it")
        }
        fixClassNameList.forEach {
            mLogger.quiet("fixClass: $it")
        }
        //生成补丁类
        createPatchClass()

        throw RuntimeException("auto patch end successfully")
    }

    private fun toCtClasses(
        transformInputs: Collection<TransformInput>,
        classPool: ClassPool
    ): List<CtClass> {
        val classNames = mutableListOf<String>()
        transformInputs.forEach {
            it.directoryInputs.forEach { dirInput ->
                val dirPath = dirInput.file.absolutePath
                classPool.insertClassPath(dirPath)
                dirInput.file.listFiles(true).asSequence()
                    .filter { it.name.endsWith(SdkConstants.DOT_CLASS) }.forEach {
                        val className = it.absolutePath.substring(
                            dirPath.length + 1,
                            it.absolutePath.length - SdkConstants.DOT_CLASS.length
                        ).replace('\\', '.')

                        mLogger.quiet("directoryInputs class: $className")
                        classNames.add(className)
                    }
            }

            it.jarInputs.forEach { jarInput ->
                classPool.insertClassPath(jarInput.file.absolutePath)
                val jarFile = JarFile(jarInput.file)
                jarFile.entries().asSequence().forEach { jarEntry ->
                    if (jarEntry.name.endsWith(SdkConstants.DOT_CLASS)) {
                        val className = jarEntry.name.substring(
                            0,
                            jarEntry.name.length - SdkConstants.DOT_CLASS.length
                        )
                            .replace('/', '.')
                        if (className.contains("META-INF", true)) {
                            mLogger.log(LogLevel.ERROR, "jarInputs class: $className")
                        } else {
                            mLogger.quiet("jarInputs class: $className")
                            classNames.add(className)
                        }
                    }
                }
            }
        }

        val allClasses = classNames.map {
            classPool.get(it)
        }.toList()
        return allClasses
    }

    private fun File.listFiles(recursive: Boolean = false): List<File> {
        val files = ArrayList<File>()
        if (this.isFile) {
            files.add(this)
            return files
        }
        if (this.isDirectory) {
            this.listFiles()?.forEach { f ->
                if (f.isDirectory) {
                    files.addAll(f.listFiles(recursive))
                } else {
                    files.add(f)
                }
            }
        }
        return files
    }

    private val FIX_CLASS_PATH = "com.billkalin.android.hot.qucikfix.common.Fix"
    private var FIX_CLASS: Class<*>? = null
    private val fixClassNameList = mutableListOf<String>()
    private val fixMethodList = mutableListOf<String>()
    private val invokeSuperMethodList = mutableMapOf<String, ArrayList<CtMethod>>()
    private val addedSuperMethodList = mutableListOf<CtMethod>()

    private fun readAnnotation(classes: List<CtClass>, logger: Logger) {
        if (FIX_CLASS == null) {
            FIX_CLASS = classes[0].classPool.get(FIX_CLASS_PATH).toClass()
        }
        classes.forEach {
            scanFixMethodAndClass(it)
        }
    }

    private fun scanFixMethodAndClass(ctClass: CtClass) {
        val patchMethodSet = mutableSetOf<String>()
        ctClass.declaredMethods.asSequence().filter { it.hasAnnotation(FIX_CLASS) }.forEach {
            addFixMethod(it, patchMethodSet)
        }
        ctClass.defrost()
        fixMethodList.addAll(patchMethodSet)
    }

    private fun addFixMethod(ctMethod: CtMethod, fixMethodSet: MutableSet<String>) {
//        val methodFix = ctMethod.getAnnotation(FIX_CLASS) as Fix
//        ctMethod.hasAnnotation(FIX_CLASS)
        fixMethodSet.add(ctMethod.longName)
        if (!fixClassNameList.contains(ctMethod.declaringClass.name)) {
            fixClassNameList.add(ctMethod.declaringClass.name)
        }
//        val fx = Fix
    }

    private fun scanFixSuperMethod() {
        //扫描是否有调用super方法的类
        fixClassNameList.forEach {
            val ctClass = classPool.get(it)
            ctClass.defrost()
            val superMethodList = invokeSuperMethodList.getOrDefault(it, ArrayList<CtMethod>())
            ctClass.declaredMethods.asSequence().filter { fixMethodList.contains(it.longName) }
                .forEach {
                    it.instrument(object : ExprEditor() {
                        override fun edit(m: MethodCall?) {
                            if (m?.isSuper == true) {
                                if (!superMethodList.contains(m.method)) {
                                    superMethodList.add(m.method)
                                }
                            }
                        }
                    })
                }
            invokeSuperMethodList[it] = superMethodList
        }
    }

    private fun clonePathClass(
        patchName: String,
        modifyClass: CtClass,
        exceptMethods: Set<CtMethod>
    ): CtClass {
        var patchClass = classPool.getOrNull(patchName)
        patchClass?.defrost()
        patchClass = classPool.makeClass(patchName)
        patchClass.classFile.majorVersion = ClassFile.JAVA_7
        patchClass.superclass = modifyClass.superclass
        modifyClass.declaredFields.forEach {
            patchClass.addField(CtField(it, patchClass))
        }
        val classMap = ClassMap()
        classMap[patchName] = modifyClass.name
        classMap.fix(modifyClass)

        modifyClass.declaredMethods.asSequence()
            .filter { !exceptMethods.contains(it) }
            .map { CtMethod(it, patchClass, classMap) }.forEach {
                patchClass.addMethod(it)
            }
        patchClass.modifiers = AccessFlag.clear(patchClass.modifiers, AccessFlag.ABSTRACT)
        return patchClass
    }

    private fun createPatchClass() {
        fixClassNameList.forEach {
            val modifyClass = classPool.get(it)
            val patchClassName =
                "${Constants.PATCH_PACKAGE_NAME}.${it.substring(it.lastIndexOf('.') + 1)}${Constants.PATCH_CLASS_SUFFIX}"

            val noNeedPatchMethod = mutableSetOf<CtMethod>()
            modifyClass.declaredMethods.forEach {
                if (fixMethodList.contains(it.longName)) {

                } else {
                    noNeedPatchMethod.add(it)
                }
            }
            //创建patch类，只包含了修改过的方法和字段
            val patchClass = clonePathClass(patchClassName, modifyClass, noNeedPatchMethod)
            //新增构造函数和被修改类的字段，传入被修改的类的对象
            handleConstructor(modifyClass, patchClass)
            //新增realParams方法
            val realParamMethod = addGetRealParamsMethod(patchClass)
            //处理调用了super方法的方法
            handleSuperMethod(modifyClass, patchClass, patchPath)
            //修改私有方法
            handlePrivateMethod(patchClass)
            //处理修改的方法的内部代码逻辑
            handleMethodInstrument(patchClass, realParamMethod, modifyClass)
            //复制patch类，移除字段
            val finalPatchClass =
                clonePathClassWithNoFields(patchClassName, patchClass, Collections.emptySet())
            //修改构造函数
            handleConstructor(modifyClass, finalPatchClass)

            //写入文件
            finalPatchClass.writeFile(patchPath)
            finalPatchClass.defrost()
        }
    }

    private fun clonePathClassWithNoFields(
        patchName: String,
        modifyClass: CtClass,
        exceptMethods: Set<CtMethod>
    ): CtClass {
        val patchClass = clonePathClass(patchName, modifyClass, exceptMethods)
        patchClass.declaredFields.forEach {
            patchClass.removeField(it)
        }
        patchClass.superclass = classPool["java.lang.Object"]
        return patchClass
    }


    private fun handleConstructor(sourceClass: CtClass, newClass: CtClass) {
        val fieldName = Constants.PATCH_ORIGIN_CLASS_FIELD_NAME
        val ctField = CtField(sourceClass, fieldName, newClass)
        newClass.addField(ctField)
        var constructBody = " public Patch(Object o) {\n"
        constructBody += "$fieldName = (${sourceClass.name})o;\n"
        constructBody += "}";
        val newConstruct = CtNewConstructor.make(constructBody, newClass)
        newClass.addConstructor(newConstruct)
    }

    /**
     * public Object[] getRealParams(Object[] args) {
     *  if(args == null || args.length < 1) {
     *      return args;
     *  }
     * Object[] realParams = (Object[])(java.lang.reflect.Array.newInstance(args.getClass().getComponentType(), args.length));
     * for(int i=0;i<args.length;i++) {
     *    if(args[i] instanceof Object[]) {
     *      realParams[i] = getRealParams((Object[]) args[i]);
     *    }else {
     *      if(args[i] == this) {
     *          realParams[i] = this.originClass;
     *        } else {
     *          realParams[i] = args[i];
     *        }
     *    }
     * }
     * return realParams;
     * }
     */
    private fun addGetRealParamsMethod(ctClass: CtClass): CtMethod {
        val methodBodyStr = StringBuilder()
        methodBodyStr.append("public Object[] ${Constants.PATCH_GET_REAL_METHOD_NAME}(Object[] args)")
            .append("{")
        methodBodyStr.append("if(args == null || args.length < 1) {")
        methodBodyStr.append("return args;")
        methodBodyStr.append("}")
        methodBodyStr.append("Object[] realParams = (Object[])(java.lang.reflect.Array.newInstance(args.getClass().getComponentType(), args.length));")
        methodBodyStr.append("for (int i=0; i<args.length; i++) {")
        methodBodyStr.append("if(args[i] instanceof Object[]) {")
        methodBodyStr.append("realParams[i] = ${Constants.PATCH_GET_REAL_METHOD_NAME}((Object[])args[i]);")
        methodBodyStr.append("} else {");
        methodBodyStr.append("if(args[i] == this) {")
        methodBodyStr.append("realParams[i] = this.${Constants.PATCH_ORIGIN_CLASS_FIELD_NAME};")
        methodBodyStr.append("} else {");
        methodBodyStr.append("realParams[i] = args[i];");
        methodBodyStr.append("}");
        methodBodyStr.append("}");
        methodBodyStr.append("}");
        methodBodyStr.append("return realParams;");
        methodBodyStr.append("}");
        val method = CtMethod.make(methodBodyStr.toString(), ctClass)
        ctClass.addMethod(method)
        return method
    }

    private fun handleSuperMethod(modifyClass: CtClass, patchClass: CtClass, patchPath: String) {
        val methods = invokeSuperMethodList.getOrDefault(modifyClass.name, ArrayList<CtMethod>())
        methods.forEach {
            val superMethodBody = StringBuilder()
            if (it.parameterTypes.isNullOrEmpty()) {
                superMethodBody.append("public static ${it.returnType.name} ${Constants.PATCH_STATIC_SUPER_METHOD_PREFIX}${it.name}(")
                superMethodBody.append("${patchClass.name} patchInstance, ${modifyClass.name} modifyInstance) {")
            } else {
                superMethodBody.append("public static ${it.returnType.name} ${Constants.PATCH_STATIC_SUPER_METHOD_PREFIX}${it.name}(")
                superMethodBody.append("${patchClass.name} patchInstance, ${modifyClass.name} modifyInstance, ")
                val paramsSignature = QuickFixUtils.paramSignatureString(it)
                superMethodBody.append(paramsSignature).append(") {")
            }

            //生成辅助类
            val assistClass =
                FixAssistFactory.createAssistClass(classPool, modifyClass, patchClass, it)
            assistClass.writeFile(patchPath)

            if (it.returnType == CtClass.voidType) {
                superMethodBody.append("${assistClass.name}.${Constants.PATCH_SUPER_METHOD_PREFIX}${it.name}(modifyInstance, patchInstance);")
            } else {
                val values = QuickFixUtils.paramValueString(it)
                superMethodBody.append("return ${assistClass.name}.${Constants.PATCH_SUPER_METHOD_PREFIX}${it.name}(modifyInstance, patchInstance, $values);")
            }
            superMethodBody.append("}")

            val superMethod = CtMethod.make(superMethodBody.toString(), patchClass)
            addedSuperMethodList.add(superMethod)
            patchClass.addMethod(superMethod)
        }
    }

    private fun handlePrivateMethod(ctClass: CtClass) {
        val privateMethodList =
            ctClass.declaredMethods.asSequence().filter { AccessFlag.isPrivate(it.modifiers) }
                .toCollection(ArrayList<CtMethod>())
        privateMethodList.asSequence().map {
            val methodBody = StringBuilder()
            methodBody.append("public ${QuickFixUtils.getMethodModifierString(it)} ${it.returnType.name} ${Constants.PATCH_PUBLIC_METHOD_PREFIX}${it.name}(")
            methodBody.append(QuickFixUtils.paramSignatureString(it)).append(")").append("{")
            methodBody.append("return ").append(it.name).append("(")
                .append(QuickFixUtils.paramValueString(it)).append(");")
            methodBody.append("}")
            CtMethod.make(methodBody.toString(), ctClass)
        }.forEach {
            ctClass.addMethod(it)
        }
    }

    private fun handleMethodInstrument(
        patchClass: CtClass,
        realParamMethod: CtMethod,
        modifyClass: CtClass
    ) {
        val invokeMethodList = invokeSuperMethodList.getOrDefault(modifyClass.name, ArrayList())
        patchClass.declaredMethods.asSequence().filter {
            !addedSuperMethodList.contains(it) && it != realParamMethod && !it.name.startsWith(
                Constants.PATCH_PUBLIC_METHOD_PREFIX
            )
        }.forEach {
            it.instrument(object : ExprEditor() {
                override fun edit(f: FieldAccess) {
                    //修改访问字段
                    if (f.isReader) {
                        f.replace(
                            QuickFixUtils.getFieldValueString(
                                f.field,
                                patchClass.name,
                                modifyClass.name
                            )
                        )
                    } else if (f.isWriter) {
                        f.replace(
                            QuickFixUtils.setFieldValueString(
                                f.field,
                                patchClass.name,
                                modifyClass.name
                            )
                        )
                    }
                }

                override fun edit(c: Cast) {
                    //类型装换
                    val methodInfo = ReflectUtils.getFieldValue(c, "thisMethod") as MethodInfo
                    val classInfo = ReflectUtils.getFieldValue(c, "thisClass") as CtClass
                    val isStatic = Modifier.isStatic(methodInfo.accessFlags)
                    if (!isStatic && !c.type.isArray) {
                        // static函数是没有this指令的，直接会报错。
                        c.replace(QuickFixUtils.getCastString(c, classInfo))
                    }
                }

                override fun edit(m: MethodCall) {
                    //方法调用
                    if (m.methodName.contains("lambdaFactory")) {
                        m.replace(
                            QuickFixUtils.getInnerClassString(
                                m.signature,
                                patchClass.name,
                                Modifier.isStatic(it.modifiers),
                                m.className
                            )
                        )
                        return
                    }
                    //
                    val replaceInlineMethod = false
                    if (!replaceInlineMethod) {
                        //
                        if (invokeMethodList.contains(m.method)) {
                            val index = invokeMethodList.indexOf(m.method)
                            val ctMethod = invokeMethodList[index]
                            if (!ctMethod.longName.isNullOrEmpty() && ctMethod.longName == m.method.longName) {
                                var firstVariable: String = ""
                                if (Modifier.isStatic(it.modifiers)) {
                                    val methodInfo = it.methodInfo
                                    val localVariableAttribute: LocalVariableAttribute =
                                        methodInfo.codeAttribute.getAttribute(LocalVariableAttribute.tag) as LocalVariableAttribute
                                    val length = localVariableAttribute.length()
                                    if (length > 0) {
                                        val firstConstantPool = localVariableAttribute.nameIndex(0)
                                        firstVariable =
                                            methodInfo.constPool.getUtf8Info(firstConstantPool)
                                    }
                                }
                                m.replace(QuickFixUtils.getInvokeSuperString(m.method, firstVariable))
                                return
                            }
                        }
                        m.replace(
                            QuickFixUtils.getMethodCallString(
                                m,
                                Modifier.isStatic(it.modifiers),
                                patchClass
                            )
                        )
                    }
                }

                override fun edit(e: NewExpr) {
                    //创建对象
                    val isStatic = Modifier.isStatic(classPool.getCtClass(e.className).modifiers)
                    if (!isStatic && isInnerClass(e.className, modifyClass.name)) {
                        e.replace(
                            QuickFixUtils.getInnerClassString(
                                e.signature,
                                patchClass.name,
                                isStatic,
                                e.className
                            )
                        )
                        return
                    }
                    e.replace(
                        QuickFixUtils.createInnerClassString(
                            e.signature,
                            patchClass.name,
                            isStatic,
                            e.className
                        )
                    )
                }
            })
        }
    }

    private fun isInnerClass(className: String, targetClass: String): Boolean {
        if (className.lastIndexOf('$') < 0)
            return false
        return className.substring(0, className.lastIndexOf('$')) == targetClass
    }
}