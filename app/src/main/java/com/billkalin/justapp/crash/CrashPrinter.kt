package com.billkalin.justapp.crash

import android.widget.Toast
import com.billkalin.android.hot.qucikfix.common.Fix
import com.billkalin.justapp.JustApp

class CrashPrinter {

    fun printText(text: String) {
        Toast.makeText(JustApp.instance, "print $text", Toast.LENGTH_SHORT).show()
//        Toast.makeText(JustApp.instance, "error", Toast.LENGTH_SHORT).show()
    }

    @Fix
    fun testQuickFix(text: String, num: Int, array: Array<String>): String {
        Toast.makeText(JustApp.instance, "testQuickFix $text", Toast.LENGTH_SHORT).show()
        return "bug fix"
    }
}