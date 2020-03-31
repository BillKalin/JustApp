package com.base.app.spi.util

import java.io.File
import java.util.concurrent.RecursiveTask

class FileTask(private val roots: Iterable<File>) : RecursiveTask<Collection<File>>() {

    constructor(files: Array<File>) : this(files.toList())

    constructor(file: File) : this(listOf(file))

    override fun compute(): Collection<File> {
        val result = mutableListOf<File>()
        val tasks = mutableListOf<RecursiveTask<Collection<File>>>()
        roots.forEach {
            if (it.isDirectory) {
                it.listFiles()?.let { files ->
                    FileTask(files.asIterable()).also { task ->
                        tasks.add(task)
                    }.fork()
                }
            } else {
                result.add(it)
            }
        }
        return result + tasks.flatMap {
            it.join()
        }
    }
}

fun File.search(): Collection<File> = FileTask(this).execute()

fun Iterable<File>.search(): Collection<File> = FileTask(this).execute()

fun Array<File>.search(): Collection<File> = FileTask(this).execute()

fun File.touch(): File {
    if (!exists()) {
        this.parentFile?.mkdirs()
        this.createNewFile()
    }
    return this
}

fun File.file(vararg path: String) = File(this, path.joinToString(File.separator))

