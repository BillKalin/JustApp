package com.base.app.spi.util

import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

fun ByteArray.inputStream(): ByteArrayInputStream = ByteArrayInputStream(this)

fun InputStream.redirect(file: File): Long = copyTo(file.touch().outputStream())

fun ByteArray.redirect(file: File): Long = this.inputStream().redirect(file)