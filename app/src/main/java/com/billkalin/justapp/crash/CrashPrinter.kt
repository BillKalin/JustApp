package com.billkalin.justapp.crash

import android.widget.Toast
import com.billkalin.justapp.JustApp

class CrashPrinter {

    fun printText(text: String) {
        Toast.makeText(JustApp.instance, "print $text", Toast.LENGTH_SHORT).show()
//        Toast.makeText(JustApp.instance, "error", Toast.LENGTH_SHORT).show()
    }

}