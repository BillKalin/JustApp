package com.billkalin.performance

import android.content.Context
import com.billkalin.performance.core.block.BlockChecker

object PerformanceApp {

    lateinit var sContext: Context

    fun init(context: Context?) {
        context ?: return
        sContext = context.applicationContext
        BlockChecker.start()
    }

}