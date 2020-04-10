package com.base.app.spi.util
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.parallel.InputStreamSupplier
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

fun File.transform(output: File, transformer: (ByteArray) -> ByteArray = { it -> it }) {
    when {
        isDirectory -> {
            toURI().let { base ->
                this.search().parallelStream().forEach {
                    it.transform(File(output, base.relativize(it.toURI()).path), transformer)
                }
            }
        }
        isFile -> {
            when (this.extension.toLowerCase()) {
                "jar" -> {
                    JarFile(this).use {
                        it.transform(output, ::ZipArchiveEntry, transformer)
                    }
                }
                "class" -> {
                    this.inputStream().use {
                        it.transform(transformer).redirect(output)
                    }
                }
                else -> {
                    this.copyTo(output, true)
                }
            }
        }
        else -> {

        }
    }
}

fun InputStream.transform(transformer: (ByteArray) -> ByteArray): ByteArray {
    return transformer(readBytes())
}

fun ZipFile.transform(
    output: OutputStream,
    entryFactory: (ZipEntry) -> ZipArchiveEntry = ::ZipArchiveEntry,
    transformer: (ByteArray) -> ByteArray = { it -> it }
) {
    val entries = mutableSetOf<String>()
    val creator = ParallelScatterZipCreator(
        ThreadPoolExecutor(
            NCPU,
            NCPU,
            0L,
            TimeUnit.MILLISECONDS,
            LinkedBlockingQueue<Runnable>(),
            Executors.defaultThreadFactory(),
            RejectedExecutionHandler { runnable, _ ->
                runnable.run()
            })
    )

    entries().asSequence().forEach { entry ->
        if (!entries.contains(entry.name)) {
            val zae = entryFactory(entry)
            val stream = InputStreamSupplier {
                when (entry.name.substringAfterLast('.', "")) {
                    "class" -> getInputStream(entry).use { src ->
                        src.transform(transformer).inputStream()
                    }
                    else -> getInputStream(entry)
                }
            }

            creator.addArchiveEntry(zae, stream)
            entries.add(entry.name)
        } else {
            System.err.println("Duplicated jar entry: ${this.name}!/${entry.name}")
        }
    }

    ZipArchiveOutputStream(output).use(creator::writeTo)
}

fun ZipFile.transform(
    output: File,
    entryFactory: (ZipEntry) -> ZipArchiveEntry = ::ZipArchiveEntry,
    transformer: (ByteArray) -> ByteArray = { it -> it }
) = output.touch().outputStream().buffered().use {
    transform(it, entryFactory, transformer)
}

fun ZipInputStream.transform(
    output: OutputStream,
    entryFactory: (ZipEntry) -> ZipArchiveEntry = ::ZipArchiveEntry,
    transformer: (ByteArray) -> ByteArray
) {
    val creator = ParallelScatterZipCreator()
    val entries = mutableSetOf<String>()

    while (true) {
        val entry = nextEntry?.takeIf { true } ?: break
        if (!entries.contains(entry.name)) {
            val zae = entryFactory(entry)
            val data = readBytes()
            val stream = InputStreamSupplier {
                transformer(data).inputStream()
            }
            creator.addArchiveEntry(zae, stream)
            entries.add(entry.name)
        }
    }

    ZipArchiveOutputStream(output).use(creator::writeTo)
}

fun ZipInputStream.transform(
    output: File,
    entryFactory: (ZipEntry) -> ZipArchiveEntry = ::ZipArchiveEntry,
    transformer: (ByteArray) -> ByteArray
) = output.touch().outputStream().buffered().use {
    transform(it, entryFactory, transformer)
}

