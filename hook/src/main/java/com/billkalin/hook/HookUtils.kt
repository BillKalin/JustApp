package com.billkalin.hook

import android.os.Handler
import com.billkalin.kreflect.KQuietReflect

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

}