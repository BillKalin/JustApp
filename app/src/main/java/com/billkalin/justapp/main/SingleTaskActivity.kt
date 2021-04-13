package com.billkalin.justapp.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.billkalin.justapp.R
import kotlinx.android.synthetic.main.activity_single_task.*

class SingleTaskActivity : AppCompatActivity() {

    private val TAG = SingleTaskActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_task)

        test()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.back_btn -> {
                setResult(RESULT_OK, Intent().apply {
                    putExtra("daqta", "test")
                })
                finish()
            }
        }
    }

    private fun test() {
        back_btn.post { Log.i(TAG, "[view.post] >>>> 1 ") }

        Handler(Looper.getMainLooper()).post {
            Log.i(
                TAG,
                "[handler.post] >>>> 2"
            )
        }

        //在主线程，直接调用了run方法
        runOnUiThread { Log.i(TAG, "[runOnUiThread] >>>>> 3") }

        Thread {
            runOnUiThread {
                Log.i(
                    TAG,
                    "[runOnUiThread from thread] >>>> 4"
                )
            }
        }.start()
    }
}