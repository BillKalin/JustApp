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
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import java.util.jar.JarFile
import kotlin.collections.ArrayList


class QuickFixTransformer(private val project: Project, private val android: AppExtension) :
    Transform() {
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
        //生成补丁类
        createPatchClass()
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

//                        mLogger.quiet("directoryInputs class: $className")
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
//                        mLogger.quiet("jarInputs class: $className")
                        classNames.add(className)
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

        fixMethodList.forEach {
            mLogger.quiet("fixMethod: $it")
        }
        fixClassNameList.forEach {
            mLogger.quiet("fixClass: $it")
        }
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

    private fun createPatchClass() {
        fixClassNameList.forEach {
            val sourceClass = classPool.get(it)
            val patchClassName =
                "${Constants.PATCH_PACKAGE_NAME}.${it.substring(it.lastIndexOf('.') + 1)}${Constants.PATCH_CLASS_SUFFIX}"

            val noNeedPatchMethod = mutableSetOf<CtMethod>()
            sourceClass.declaredMethods.forEach {
                if (fixMethodList.contains(it.longName)) {

                } else {
                    noNeedPatchMethod.add(it)
                }
            }
            //创建patch类，只包含了修改过的方法和字段
            val newClass = clonePathClass(patchClassName, sourceClass, noNeedPatchMethod)
            //新增构造函数和被修改类的字段，传入被修改的类的对象
            addPatchConstructor(sourceClass, newClass)
            //新增realParams方法
            addGetRealParamsMethod(newClass)
            //处理调用了super方法的方法
            dealSuperMethod(sourceClass, newClass, patchPath)
            //修改内联方法

            //处理修改的方法的内部代码逻辑

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

    private fun addPatchConstructor(sourceClass: CtClass, newClass: CtClass) {
        val fieldName = Constants.PATCH_ORIGIN_CLASS_FIELD_NAME
        val ctField = CtField(sourceClass, fieldName, sourceClass)
        newClass.addField(ctField)
        var constructBody = " public Patch(Object o) {\n"
        constructBody += "fieldName = (${sourceClass.name})o;\n"
        constructBody += "}";
        val newConstruct = CtNewConstructor.make(constructBody, newClass)
        newClass.addConstructor(newConstruct)
    }

    private fun addGetRealParamsMethod(ctClass: CtClass) {
        val methodBodyStr = StringBuilder()
        methodBodyStr.append("public Object[] getRealParams(Object[] args)").append("{")
        methodBodyStr.append("if(args == null || args.length < 1) {")
        methodBodyStr.append("return args;")
        methodBodyStr.append("}")
        methodBodyStr.append("Object[] realParams = (Object[])new java.lang.reflect.Array.newInstance(args.getClass().getComponentType(), args.length);")
        methodBodyStr.append("for (int i=0; i<args.length; i++) {")
        methodBodyStr.append("if(args[i] instanceOf Object[]) {")
        methodBodyStr.append("realParams = getRealParams((Object[])args[i]);")
        methodBodyStr.append("} else {");
        methodBodyStr.append("if(args[i]) == this) {")
        methodBodyStr.append("realParams[i] = this.${Constants.PATCH_ORIGIN_CLASS_FIELD_NAME};");
        methodBodyStr.append("} else {");
        methodBodyStr.append("realParams[i] = args[i];");
        methodBodyStr.append("}");
        methodBodyStr.append("}");
        methodBodyStr.append("}");
        methodBodyStr.append("return realParams;");
        methodBodyStr.append("}");
        val method = CtMethod.make(methodBodyStr.toString(), ctClass)
        ctClass.addMethod(method)
    }

    private fun dealSuperMethod(sourceClass: CtClass, newClass: CtClass, patchPath: String) {
        val methods = invokeSuperMethodList.getOrDefault(sourceClass.name, ArrayList<CtMethod>())
        methods.forEach {
            val superMethodBody = StringBuilder()
            val paramValues = StringBuilder()
            if (it.parameterTypes.isNullOrEmpty()) {
                superMethodBody.append("public static ${it.returnType.name} staticFix${it.name}(")
                superMethodBody.append("${newClass.name} patchInstance, ${sourceClass.name} fixInstance) {")
            } else {
                superMethodBody.append("public static ${it.returnType.name} staticFix${it.name}(")
                superMethodBody.append("${newClass.name} patchInstance, ${sourceClass.name} fixInstance, ")
                val paramStringBuilder = StringBuilder()
                it.parameterTypes.forEachIndexed { index, cls ->
                    paramStringBuilder.append("${cls.name} var$index").append(", ")
                    paramValues.append("var$index").append(",")
                }
                val param =
                    paramStringBuilder.substring(0, paramStringBuilder.toString().length - 1)
                superMethodBody.append(param).append(") {")
            }

            //生成辅助类
            val assistClass =
                FixAssistFactory.createAssistClass(classPool, sourceClass, newClass, it)
            assistClass.writeFile(patchPath)

            if (it.returnType == CtClass.voidType) {
                superMethodBody.append("${assistClass.name}.${Constants.PATCH_SUPER_METHOD_PREFIX}${it.name}(fixInstance, patchInstance);")
            } else {
                val values = paramValues.substring(0, paramValues.length - 1)
                superMethodBody.append("return ${assistClass.name}.${Constants.PATCH_SUPER_METHOD_PREFIX}${it.name}(fixInstance, patchInstance, $values);")
            }
            superMethodBody.append("}")

            val superMethod = CtMethod.make(superMethodBody.toString(), newClass)
            newClass.addMethod(superMethod)
        }
    }
}