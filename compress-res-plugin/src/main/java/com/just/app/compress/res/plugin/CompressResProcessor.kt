package com.just.app.compress.res.plugin

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.ProcessAndroidResources
import com.base.app.spi.VariantProcessor
import com.base.app.spi.transformer.ArtifactManager
import com.base.app.spi.util.*
import com.google.auto.service.AutoService
import com.just.app.plugin.compress.res.plugin.Build
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@AutoService(VariantProcessor::class)
public class CompressResProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        variant.project.tasks.withType(ProcessAndroidResources::class.java)
            .findByName("process${variant.name.capitalize()}Resources")?.doLast {
                variant.artifacts.get(ArtifactManager.PROCESSED_RES).search { file ->
                    file.name.startsWith(SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP) && file.extension == SdkConstants.EXT_RES
                }.parallelStream().forEach {
                    println("file src size = ${it.length()}")
                    val srcFileSize = it.length()
                    it.zipRes {
                        !NO_COMPRESS.contains(it.name.substringAfter('.'))
                    }
                    val dstFileSize = it.length()
                    val logger = File(variant.project.buildDir, "reports").file(Build.ARTIFACT)
                        .file(variant.name).file("reports.txt").touch().printWriter()
                    logger.println("compress resources size: before = $srcFileSize, after = $dstFileSize, reduce size = ${srcFileSize - dstFileSize}")
                    logger.close()
                }
            }
    }

    internal val NO_COMPRESS = setOf(
        "jpg", "jpeg", "png", "gif", "webp",
        "wav", "mp2", "mp3", "ogg", "aac",
        "mpg", "mpeg", "mid", "midi", "smf", "jet",
        "rtttl", "imy", "xmf", "mp4", "m4a",
        "m4v", "3gp", "3gpp", "3g2", "3gpp2",
        "amr", "awb", "wma", "wmv", "webm", "mkv"
    )

    private fun File.zipRes(needCompress: (ZipEntry) -> Boolean) {
        val tempFile = File.createTempFile(
            SdkConstants.FN_RES_BASE + SdkConstants.RES_QUALIFIER_SEP,
            SdkConstants.DOT_RES
        )
        ZipFile(this).use {
            it.transform(tempFile, { origin ->
                ZipArchiveEntry(origin).apply {
                    method = if (needCompress(this)) ZipEntry.DEFLATED else origin.method
                }
            })
        }

        if (delete()) {
            tempFile.copyTo(this, true)
        }
    }
}
