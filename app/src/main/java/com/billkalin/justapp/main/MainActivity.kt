package com.billkalin.justapp.main

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.billkalin.justapp.R
import com.billkalin.reflect.api.ReflectApi
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Thread.sleep(5000L)

        open_api.setOnClickListener {
            val result = ReflectApi.openAllMethod()//ReflectApi.openApi(Build.VERSION_CODES.P) == 0
            Toast.makeText(
                this,
                if (result) "open hidden api success!" else "open hidden api failed!! result = $result",
                Toast.LENGTH_SHORT
            ).show()
        }

        call_api.setOnClickListener {
            if (callHiddenApi()) {
                Toast.makeText(
                    this,
                    "call hidden api success!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun callHiddenApi(): Boolean {
        try {
            val method = Activity::class.java.getDeclaredMethod("canStartActivityForResult")
            val ret = method.invoke(this)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }
}
