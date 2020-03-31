package com.base.app.spi.util

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

val NCPU = Runtime.getRuntime().availableProcessors()

fun <T> ForkJoinTask<T>.execute(): T {
    val forkPool = ForkJoinPool()
    val ret = forkPool.invoke(this)
    forkPool.shutdown()
    return ret
}

fun <T> ForkJoinTask<Collection<T>>.iterator(): Iterator<T> = this.execute().iterator()

fun <T> ForkJoinTask<Collection<T>>.forEach(action: (T) -> Unit) = this.execute().forEach(action)

fun <T> ForkJoinTask<Collection<T>>.filter(predicate: (T) -> Boolean) = this.execute().filter(predicate)

fun <T, R> ForkJoinTask<Collection<T>>.map(transform: (T) -> R) = this.execute().map(transform)
