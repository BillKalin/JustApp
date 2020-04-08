package com.just.app.r.inline.plugin

import java.io.File

abstract class Symbols<out T>(val type: String, val name: String, val value: T) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class IntSymbols(type: String, name: String, value: Int) : Symbols<Int>(type, name, value) {
    override fun toString(): String {
        return "int : type = $type, name = $name, value = $value"
    }
}

class IntArraySymbols(type: String, name: String, value: IntArray) :
    Symbols<IntArray>(type, name, value) {
    override fun toString(): String =
        "int[] $type $name ${value.joinToString(", ", "{ ", " }") { "0x${it.toString(16)}" }}"
}

object SymbolsList {

    @JvmStatic
    fun parseRtxtFile(file: File): List<Symbols<*>> {
        val symbols = mutableListOf<Symbols<*>>()
        file.forEachLine { line ->
            val sp1 = line.indexOf(' ')
            val dataType = line.substring(0, sp1)
            when (dataType) {
                "int" -> {
                    val sp2 = line.indexOf(' ', startIndex = sp1 + 1)
                    val type = line.substring(sp1 + 1, sp2)
                    val sp3 = line.indexOf(' ', startIndex = sp2 + 1)
                    val name = line.substring(sp2 + 1, sp3)
                    val value = line.substring(startIndex = sp3 + 1)
                    symbols.add(IntSymbols(type = type, name = name, value = value.toInt()))
                }
                "int[]" -> {
                    val sp2 = line.indexOf(' ', startIndex = sp1 + 1)
                    val type = line.substring(sp1 + 1, sp2)
                    val sp3 = line.indexOf(' ', startIndex = sp2 + 1)
                    val name = line.substring(sp2 + 1, sp3)

                    val values =
                        line.substring(sp3 + 1).replace('{', ' ').replace('}', ' ').split(',')
                            .map {
                                it.trim()
                            }.filter { it.isNotEmpty() }.map {
                                it.toInt()
                            }.toIntArray()
                    symbols.add(IntArraySymbols(type = type, name = name, value = values))
                }
            }
        }
        return symbols
    }

    private fun String.toInt(): Int {
        return if (startsWith("0x")) {
            substring(2).toInt(16)
        } else {
            toInt(10)
        }
    }

    fun List<Symbols<*>>.getInt(type: String, name: String): Int {
        return find { it is IntSymbols && it.type == type && it.name == name }?.value as Int
    }
}
