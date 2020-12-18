package com.billkalin.xnative.xhook.wrapper

object IoMonitorUtils {

    private const val MAX_STACK_TRACE_LAYER = 10

    fun stackTraceToString(throwable: Throwable, ignorePkg: String = ""): String {
        val stackTraceElements = throwable.stackTrace
        val stackTraceElementList = stackTraceElements.filter {
            val className = it.className
            // remove unused stacks
            !(className.contains("libcore.io")
                    || className.contains("com.tencent.matrix.iocanary")
                    || className.contains("java.io")
                    || className.contains("dalvik.system")
                    || className.contains("android.os")
                    )
        }.toMutableList()

        if (stackTraceElementList.size > MAX_STACK_TRACE_LAYER && ignorePkg.isNotEmpty()) {
            val iterator = stackTraceElementList.listIterator(stackTraceElementList.size)
            while (iterator.hasPrevious()) {
                val pre = iterator.previous().className
                if (!pre.contains(ignorePkg)) {
                    iterator.remove()
                }

                if (stackTraceElementList.size <= MAX_STACK_TRACE_LAYER) {
                    break
                }
            }
        }

        val stringBuffer = StringBuffer()
        stackTraceElementList.forEach {
            stringBuffer.append(it).append("\n")
        }
        return stringBuffer.toString()
    }
}