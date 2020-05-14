package com.android.aapt2

import com.android.aapt2.proto.Resources
import com.android.aapt2.proto.ResourcesInternal
import java.io.Closeable
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

const val MAGIC = 0x54504141
const val RES_TABLE = 0x00000000
const val RES_FILE = 0x00000001

val File.metaData : MetaData
    get() = BinaryParser(this).use {
        val magic = it.readInt()
        it.seek(0)
        when (magic) {
            MAGIC -> {
                val header = it.parseHeader()
                val type = it.readInt()
                val dataSize = it.readLong()
                when(type) {
                    RES_FILE -> {
                        it.parseResFileMetaData()
                    }
                    RES_TABLE -> {
                        println("unsupport file type = $type & header = $header, path = $absolutePath")
                        TODO()
                    }
                    else -> {
                        println("unsupport file type = $type & header = $header, path = $absolutePath")
                        TODO()
                    }
                }

            }
            RES_FILE -> {
                it.parseResFile()
                TODO()
            }
            else -> {
                TODO()
            }
        }
    }

fun BinaryParser.parseHeader(): Header {
    val magic = readInt()
    if (magic != MAGIC)
        throw Exception("invalid flat file")
    val version = readInt()
    val count = readInt()
    return Header(magic, version, count)
}

fun BinaryParser.parseResFileMetaData(): MetaData {
    val headerSize = readInt()
    val dataSize = readLong()
    val data = readBytes(headerSize)
    parse {
        ResourcesInternal.CompiledFile.parseFrom(data)
    }.let {
        val name = it.resourceName
        val sourcePath = it.sourcePath
        val configuration = it.config
        return MetaData(name, sourcePath, configuration)
    }
}

fun BinaryParser.parseResFile(): MetaData {
    val headerSize = readInt()
    val dataSize = readLong()
    val data = readBytes(headerSize)
    val compiledFile = ResourcesInternal.CompiledFile.parseFrom(data)
    return when (compiledFile.type) {
        Resources.FileReference.Type.PNG -> {
            val name = compiledFile.resourceName
            val sourcePath = compiledFile.sourcePath
            val configuration = compiledFile.config
            MetaData(name, sourcePath, configuration)
        }
        Resources.FileReference.Type.PROTO_XML -> {
//            val xmlNode = Resources.XmlNode.parseFrom(readBytes(dataSize.toInt()))
            MetaData(compiledFile.resourceName, compiledFile.sourcePath, compiledFile.config)
        }
        Resources.FileReference.Type.UNRECOGNIZED -> {
            MetaData(compiledFile.resourceName, compiledFile.sourcePath, compiledFile.config)
        }
        Resources.FileReference.Type.UNKNOWN -> {
            val sourcePath = compiledFile.sourcePath
            when (sourcePath.substringAfter(".")) {
                "png", "9.png" -> {

                }
            }
            MetaData(compiledFile.resourceName, compiledFile.sourcePath, compiledFile.config)
        }
        Resources.FileReference.Type.BINARY_XML -> {
            MetaData(compiledFile.resourceName, compiledFile.sourcePath, compiledFile.config)
        }
        else -> {
            TODO("")
        }
    }
}

class Header(val magic: Int, val version: Int, val count: Int) {
    override fun toString(): String {
        return "Header(magic=$magic, version=$version, count=$count)"
    }
}

class BinaryParser(private val file: File) : Closeable {

    private val fileChannel: FileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
    private val byteBuffer: ByteBuffer
    private val _filePath: String

    init {
        byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            .order(ByteOrder.LITTLE_ENDIAN)
        _filePath = file.absolutePath
    }

    fun readByte(): Byte = byteBuffer.get()
    fun readInt(): Int = byteBuffer.int
    fun readLong(): Long = byteBuffer.long
    fun readBytes(size: Int) = ByteArray(size).also {
        byteBuffer.get(it)
    }

    fun seek(pos: Int) {
        byteBuffer.position(pos)
    }

    fun <T> parse(handle: (ByteBuffer) -> T) = handle(byteBuffer)

    override fun close() {
        fileChannel.close()
        byteBuffer.clear()
    }

    val filePath: String
        get() = _filePath
}