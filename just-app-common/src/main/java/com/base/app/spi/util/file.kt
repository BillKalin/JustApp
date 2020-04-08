package com.base.app.spi.util

import java.io.File
import java.util.concurrent.RecursiveTask

class FileTask(
    private val roots: Iterable<File>,
    private val filter: (File) -> Boolean = { true }
) : RecursiveTask<Collection<File>>() {

    constructor(files: Array<File>, filter: (File) -> Boolean = { true }) : this(
        files.toList(),
        filter
    )

    constructor(file: File, filter: (File) -> Boolean = { true }) : this(listOf(file), filter)

    override fun compute(): Collection<File> {
        val result = mutableListOf<File>()
        val tasks = mutableListOf<RecursiveTask<Collection<File>>>()
        roots.forEach {
            if (it.isDirectory) {
                it.listFiles()?.let { files ->
                    FileTask(files.asIterable(), filter).also { task ->
                        tasks.add(task)
                    }.fork()
                }
            } else {
                if (filter.invoke(it)) {
                    result.add(it)
                }
            }
        }
        return result + tasks.flatMap {
            it.join()
        }
    }
}

fun File.search(filter: (File) -> Boolean = { true }): Collection<File> =
    FileTask(this, filter).execute()

fun Iterable<File>.search(filter: (File) -> Boolean = { true }): Collection<File> =
    FileTask(this, filter).execute()

fun Array<File>.search(filter: (File) -> Boolean = { true }): Collection<File> =
    FileTask(this, filter).execute()

fun File.touch(): File {
    if (!exists()) {
        this.parentFile?.mkdirs()
        this.createNewFile()
    }
    return this
}

fun File.file(vararg path: String) = File(this, path.joinToString(File.separator))

