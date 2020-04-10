package com.just.app.r.inline.plugin

import com.base.app.asm.ClassTransformer
import com.base.app.spi.transformer.ArtifactManager
import com.base.app.spi.transformer.ArtifactManager.Companion.SYMBOL_LIST
import com.base.app.spi.transformer.TransformContext
import com.base.app.spi.util.asIterable
import com.base.app.spi.util.file
import com.base.app.spi.util.touch
import com.google.auto.service.AutoService
import com.just.app.plugin.r.inline.plugin.Build
import com.just.app.r.inline.plugin.SymbolsList.getInt
import com.just.app.r.inline.plugin.SymbolsList.parseRtxtFile
import jdk.internal.org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import java.io.PrintWriter


@AutoService(ClassTransformer::class)
public class RInlineTransform : ClassTransformer {

    companion object {
        const val R_STYLEABLE = "R\$styleable"
        const val PRFIX_ANDROID_CONSTRAIT_LAYOUT =
            " com.android.support.constraint/constraint-layout"
        const val PRFIX_ANDROIDX_CONSTRAIT_LAYOUT =
            " androidx.constraintlayout.widget/constraintlayout"

        internal const val COM_ANDROID_INTERNAL_R = "com/android/internal/R$"
        internal const val ANDROID_R = "android/R$"
    }

    private lateinit var appPkg: String
    private lateinit var logger: PrintWriter
    private lateinit var symbols: List<Symbols<*>>
    private lateinit var appRStylable: String

    override fun onPreTransform(context: TransformContext) {
        super.onPreTransform(context)
        this.appPkg = context.originalApplicationId.replace(".", "/")
        appRStylable = "$appPkg/$R_STYLEABLE"
        this.logger =
            context.reportsDir.file(Build.ARTIFACT).file(context.name).file("report.txt").touch()
                .printWriter()

        //解析app/build/intermidiates/runtim_synmbol_list/R.txt 文件
        symbols = parseRtxtFile(context.artifacts.get(SYMBOL_LIST).single())

        val clsPath = context.compileClasspath.map {
            it.absolutePath
        }
        if (clsPath.any {
                it.contains(PRFIX_ANDROID_CONSTRAIT_LAYOUT) || it.contains(
                    PRFIX_ANDROIDX_CONSTRAIT_LAYOUT
                )
            }) {
            context.artifacts.get(ArtifactManager.MERGED_RES)
        }

        //remove R.class file
        //不兼容AGP 3.6.1+ 版本
        /*val rFiles = context.getRClassFile()
        rFiles.forEach { pair ->
            pair.first.delete()
            this.logger.println("remove name = ${pair.second} & file => ${pair.first.absolutePath}")
        }*/
    }

   /* private fun TransformContext.getRClassFile(): List<Pair<File, String>> {
        return artifacts.get(ArtifactManager.ALL_CLASSES).map { classes ->
            val baseUri = classes.toURI()
            classes.search { r ->
                r.name.startsWith("R") && r.name.endsWith(".class") && (r.name[1] == '$' || r.name.length == 7)
            }.map { r ->
                r to baseUri.relativize(r.toURI()).path.substringBefore(".class")
            }
        }.flatten().filter {
            it.second != appRStylable
        }.filter {
            true
        }
    }*/

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (symbols.isEmpty())
            return super.transform(context, klass)
        val clsname = klass.name.substring(klass.name.lastIndexOf("/") + 1)
        if (clsname.startsWith("R\$")) {
            //保留app R$styleable
            if (klass.name == appRStylable) {
                return klass
            }
            klass.fields.forEach {
                this.logger.println("delete R class field : classname = ${it.name} && ${it.desc}")
            }
            klass.fields.clear()
            return klass
        }
        klass.replaceRWithConstant()
        return klass
    }

    override fun onPostTransform(context: TransformContext) {
        super.onPostTransform(context)
        this.logger.println("onPostTransform()")
        println("onPostTransform() ")
        this.logger.close()
    }

    private fun ClassNode.replaceRWithConstant() {
        methods.forEach { methods ->
            val fields = methods.instructions.iterator().asIterable().filter {
                it.opcode == Opcodes.GETSTATIC
            }.map {
                it as FieldInsnNode
            }.filter {
                ("I" == it.desc || "[I" == it.desc)
                        && it.owner.substring(it.owner.lastIndexOf('/') + 1).startsWith("R$")
                        && !(it.owner.startsWith(COM_ANDROID_INTERNAL_R) || it.owner.startsWith(
                    ANDROID_R
                ))
            }

            val intFields = fields.filter { it.desc == "I" }
            val intArrayFields = fields.filter { it.desc == "[I" }

            intFields.forEach { field ->
                val type = field.owner.substring(field.owner.lastIndexOf("/R\$") + 3)
                try {
                    methods.instructions.insertBefore(
                        field,
                        LdcInsnNode(symbols.getInt(type, field.name))
                    )
                    methods.instructions.remove(field)

                    logger.println(
                        " * ${field.owner}.${field.name} => ${symbols.getInt(
                            type,
                            field.name
                        )}: $name.${methods.name}${methods.desc}"
                    )
                } catch (e: Exception) {
                    logger.println(" ! Unresolvable symbol `${field.owner}.${field.name}`: $name.${methods.name}${methods.desc}")
                }
            }

            intArrayFields.forEach { field ->
                logger.println("${field.owner}.name = ${field.name}")
                field.owner =
                    "$appPkg/${field.owner.substring(field.owner.lastIndexOf('/') + 1)}"
            }
        }
    }

//    private val _logger = Logging.getLogger(RInlineTransform::class.java)
}
