package com.billkalin.hook

import android.app.Activity
import android.app.Instrumentation
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import com.billkalin.kreflect.KQuietReflect
import kotlin.system.measureTimeMillis

object HookUtils {
    fun hookActivityInstrumentation() {
        try {
            val reflect = KQuietReflect.on("android.app.ActivityThread")
            //拿到ActivityThread 对象
            val currActThread = reflect.method("currentActivityThread")
                .call<Any>()
            //反射拿到Handler对象
            val oriHandler = reflect.field("mH").get<Handler>(currActThread)!!
            val handlerCallBack = KQuietReflect.on(Handler::class.java).field("mCallback")
            //拿到Handler.Callback 对象
            val oriCallback = handlerCallBack.get<Handler.Callback>(oriHandler)
            val proxyHandlerProxy = HandlerCallbackProxy(oriHandler, oriCallback)
            //替换代理Handler callback
            handlerCallBack[oriHandler] = proxyHandlerProxy
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }

    fun hookInstrumentation() {
        val reflect = KQuietReflect.on("android.app.ActivityThread")
        //拿到ActivityThread 对象
        val currActThread = reflect.method("currentActivityThread").call<Any>()
        val mInstrumentation = reflect.field("mInstrumentation").get<Instrumentation>(currActThread)
        val newInstrumentation = NewInstrumentation(mInstrumentation!!)
        reflect.field("mInstrumentation")[currActThread] = newInstrumentation
    }

    private class NewInstrumentation(private val instrumentation: Instrumentation) :
        Instrumentation() {
        private val TAG = "NewInstrumentation"
        override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?) {
            val time = measureTimeMillis {
                instrumentation.callActivityOnCreate(activity, icicle)
            }
            Log.d(TAG, "${activity!!.javaClass.canonicalName}.onCreate():$time ms")
        }

        override fun callActivityOnStart(activity: Activity?) {
            val time = measureTimeMillis {
                instrumentation.callActivityOnStart(activity)
            }
            Log.d(TAG, "${activity!!.javaClass.canonicalName}.onStart():$time ms")
        }

        override fun callActivityOnResume(activity: Activity?) {
            val time = measureTimeMillis {
                instrumentation.callActivityOnResume(activity)
            }
            Log.d(TAG, "${activity!!.javaClass.canonicalName}.onResume():$time ms")
        }

        override fun callActivityOnPause(activity: Activity?) {
            val time = measureTimeMillis {
                super.callActivityOnPause(activity)
            }
            Log.d(TAG, "${activity!!.javaClass.canonicalName}.onRause():$time ms")
        }

        override fun callActivityOnStop(activity: Activity?) {
            val time = measureTimeMillis {
                instrumentation.callActivityOnStop(activity)
            }
            Log.d(TAG, "${activity!!.javaClass.canonicalName}.onStop():$time ms")
        }

        override fun callActivityOnDestroy(activity: Activity?) {
            val time = measureTimeMillis {
                instrumentation.callActivityOnDestroy(activity)
            }
            Log.d(TAG, "${activity!!.javaClass.canonicalName}.onDestroy():$time ms")
        }
    }
}