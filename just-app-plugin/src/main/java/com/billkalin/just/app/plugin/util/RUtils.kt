package com.billkalin.just.app.plugin.util

import com.android.SdkConstants.DOT_CLASS
import org.apache.commons.io.IOUtils
import org.gradle.api.logging.Logging
import org.objectweb.asm.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

object RUtils {
    private val logger = Logging.getLogger(RUtils::class.java)
    private val rMap = mutableMapOf<String, Int>()
    private val R_CLASS_EXPR = ".*/R.class|.*/R\\$.*.class".toRegex()
    private val R_STYLEABLE_EXP = ".*/R\\$(?!styleable).*?\\.class|.*/R\\.class".toRegex()

    fun reset() {
        rMap.clear()
    }

    fun collectClassFileInfo(file: File) {
        if (DOT_CLASS != file.extension || !isRClass(file.absolutePath))
            return
//        println("collectClassFileInfo: className = ${file.absolutePath}")
        val classReader = ClassReader(file.absolutePath)
        val className = classReader.className
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val classVisitor = object : ClassVisitor(Opcodes.ASM7, classWriter) {
            override fun visitField(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                value: Any?
            ): FieldVisitor {
                if (value is Int) {
                    rMap["$className$name"] = value
                    logger.info("collectClassFileInfo: path = $className$name")
                    println("collectClassFileInfo: path = $className$name")
                }
                return super.visitField(access, name, descriptor, signature, value)
            }
        }
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
    }

    fun collectJarFileInfo(bytes: ByteArray): ByteArray {
        val classReader = ClassReader(bytes)
        val className = classReader.className
        if (!isRClass(className + DOT_CLASS)) {
            return bytes
        }
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val classVisitor = object : ClassVisitor(Opcodes.ASM7, classWriter) {
            override fun visitField(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                value: Any?
            ): FieldVisitor {
                if (value is Int) {
                    rMap["$className$name"] = value
                    println("collectJarFileInfo: path = $className$name, value = ${rMap["$className$name"]}")
                    logger.info("collectJarFileInfo: path = $className$name, value = ${rMap["$className$name"]}")
                }
                return super.visitField(access, name, descriptor, signature, value)
            }
        }
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    fun replaceRFileAndDelete(file: File) {
//        println("replaceRAndDelete: path = ${file.absolutePath}")
        if (!file.absolutePath.endsWith(DOT_CLASS))
            return
        val isStyleClass = isRStyleableClass(file.absolutePath)
        val isRClass = isRClass(file.absolutePath)
        println("file.absolutePath = $file.absolutePath, isStyleClass = $isStyleClass, isRClass = $isRClass")
        if (isRStyleableClass(file.absolutePath)) {
            println("replaceRAndDelete：isRStyleableClass -> ${file.absolutePath}")
            FileInputStream(file).use { fis ->
                val classReader = ClassReader(fis.readBytes())
                val clsName = classReader.className
                val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                val classVisitor = object : ClassVisitor(Opcodes.ASM7, classWriter) {
                    override fun visitField(
                        access: Int,
                        name: String?,
                        descriptor: String?,
                        signature: String?,
                        value: Any?
                    ): FieldVisitor? {
                        if (value is Int) {
                            println("delete styleable class field : clsName = $clsName, name = $name")
                            return null
                        }
                        return super.visitField(access, name, descriptor, signature, value)
                    }
                }
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)

                val newData = classWriter.toByteArray()
                val newFile = File(file.parentFile, file.name + ".tmp")
                FileOutputStream(newFile).apply {
                    write(newData)
                    close()
                }
                fis.close()
                file.delete()
                newFile.renameTo(file)
            }
            return
        }
        if (isRClass(file.absolutePath)) {
            println("replaceRAndDelete：R.class -> ${file.absolutePath}")
            //如果是R.class文件，删除static final int 的变量
            FileInputStream(file).use { fis ->
                val classReader = ClassReader(fis.readBytes())
                val clsName = classReader.className
                val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                val clsVisitor = object : ClassVisitor(Opcodes.ASM7, classWriter) {
                    override fun visitField(
                        access: Int,
                        name: String?,
                        descriptor: String?,
                        signature: String?,
                        value: Any?
                    ): FieldVisitor? {
                        if (value is Int) {
                            //删除
                            println("delete class $clsName -> field $name")
                            return null
                        }
                        return super.visitField(access, name, descriptor, signature, value)
                    }
                }
                classReader.accept(clsVisitor, ClassReader.EXPAND_FRAMES)

                val newData = classWriter.toByteArray()
                val newFile = File(file.parentFile, file.name + ".tmp")
                FileOutputStream(newFile).apply {
                    write(newData)
                    close()
                }
                fis.close()
                file.delete()
                newFile.renameTo(file)
            }
        } else {
            println("replaceRAndDelete: class -> ${file.absolutePath}")
            //如果不是R文件
            FileInputStream(file).use { fis ->
                val data = fis.readBytes()
                val newData = replaceRClass(data)
                val newFile = File(file.parentFile, file.name + ".tmp")
                FileOutputStream(newFile).apply {
                    write(newData)
                    close()
                }
                fis.close()
                file.delete()
                newFile.renameTo(file)
            }
        }
    }

    fun replaceAndDeleteRInfoFromJar(srcFile: File): File {
        val tempFile = File(srcFile.parentFile, srcFile.name + ".temp").apply {
            if (exists()) delete()
        }
        val jarOutputStream = JarOutputStream(FileOutputStream(tempFile))
        val jarFile = JarFile(srcFile)
        jarFile.stream().forEach { jarEntry ->
            val zipEntry = ZipEntry(jarEntry.name)
            val jarInputStream = jarFile.getInputStream(zipEntry)
            var classByteArray = IOUtils.toByteArray(jarInputStream)
            if (zipEntry.name.endsWith(DOT_CLASS)) {
                classByteArray = replaceRClass(classByteArray)
            }
            if(classByteArray.isNotEmpty()) {
                jarOutputStream.putNextEntry(zipEntry)
                jarOutputStream.write(classByteArray)
                jarOutputStream.closeEntry()
            }
        }
        jarOutputStream.close()
        return tempFile
    }

    /**
     * 替换内连R文件字段
     */
    fun replaceRClass(bytes: ByteArray): ByteArray {
        val classReader = ClassReader(bytes)
        val clsName = classReader.className
        val isStyleClass = isRStyleableClass(clsName + DOT_CLASS)
        val isRClass = isRClass(clsName + DOT_CLASS)
        println("replaceRClass : clsName = $clsName, isRClass = $isRClass, isStyleableClass = $isStyleClass")
        if(isRClass && !isStyleClass) {
            println("delete R class $clsName ->>>>>>>>>>")
//            return ByteArray(0)
        }
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val classVisitor = object : ClassVisitor(Opcodes.ASM7, classWriter) {

            override fun visitField(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                value: Any?
            ): FieldVisitor? {
                if ((isRClass || isStyleClass) && (value is Int)) {
                    println("replace ::: clsName = $clsName, owner.name = $name value = $value   -> removed!!")
                    return null
                }
                return super.visitField(access, name, descriptor, signature, value)
            }

            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                val srcMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
                return object : MethodVisitor(Opcodes.ASM7, srcMethod) {
                    override fun visitFieldInsn(
                        opcode: Int,
                        owner: String?,
                        name: String?,
                        descriptor: String?
                    ) {
                        val key = "$owner$name"
                        val value = rMap["$owner$name"]
                        if (value != null) {
                            println("replace ::: clsName = $clsName, owner.name = $key, value = $value")
                            logger.info("replace ::: clsName = $clsName, owner.name = $key, value = $value")
                            super.visitLdcInsn(value)
                        } else {
                            super.visitFieldInsn(opcode, owner, name, descriptor)
                        }
                    }
                }
            }
        }
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    private fun isRClass(path: String): Boolean = R_CLASS_EXPR.matches(path)
    private fun isRStyleableClass(path: String): Boolean = R_STYLEABLE_EXP.matches(path)
}