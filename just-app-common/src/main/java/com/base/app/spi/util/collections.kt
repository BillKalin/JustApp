package com.base.app.spi.util

fun <T> Iterator<T>.asIterable() = Iterable { this }