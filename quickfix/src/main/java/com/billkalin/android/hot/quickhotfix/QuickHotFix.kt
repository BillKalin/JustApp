package com.billkalin.android.hot.quickhotfix

interface QuickHotFix {

    fun dispatch(methodname: String, parameterName: Array<*>): Any?

}