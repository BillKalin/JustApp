package com.billkalin.android.hot.qucikfix.common

import java.lang.reflect.Field
import java.lang.reflect.Method

object ReflectUtils {

    @JvmStatic
    fun getField(name: String, clazz: Class<*>): Field? {
        val field = clazz.getDeclaredField(name)
        if (field != null) {
            field.isAccessible = true
        }
        return field
    }

    @JvmStatic
    fun getStaticFieldValue(name: String, clazz: Class<*>): Any? {
        val field = getField(name, clazz)
        return field!!.get(null)
    }

    @JvmStatic
    fun getFieldValue(name: String, instance: Any, clazz: Class<*>): Any? {
        val field = getField(name, clazz)
        return field!!.get(instance)
    }

    @JvmStatic
    fun setStaticFieldValue(name: String, value: Any?, clazz: Class<*>): Any? {
        val field = getField(name, clazz)
        return field!!.set(null, value)
    }

    @JvmStatic
    fun setFieldValue(name: String, value: Any?, instance: Any?, clazz: Class<*>): Any? {
        val field = getField(name, clazz)
        return field!!.set(instance, value)
    }

    @JvmStatic
    fun reflectConstruct(className: String, params: Array<*>, args: Array<Class<*>>): Any? {
        try {
            val clazz = Class.forName(className)
            val constructor = clazz.getDeclaredConstructor(*args)
            return constructor.newInstance(params)
        }catch (e: Exception) {
            throw e
        }
        return null
    }

    @JvmStatic
    fun invokeStaticMethod(name: String, clazz: Class<*>, paramsClass: Array<Class<*>>, args: Array<Any?>): Any? {
        val method = try {
            clazz.getDeclaredMethod(name, *paramsClass)
        }catch (e: NoSuchMethodException) {
            null
        }
        method?.let {
            it.isAccessible = true
            return it.invoke(null, *args)
        }
        return null
    }

    @JvmStatic
    fun invokeMethod(name: String, clazz: Class<*>, instance: Any?, paramsClass: Array<Class<*>>, args: Array<Any?>): Any? {
        val method = try {
            getMethod(name, clazz, paramsClass)
        }catch (e: NoSuchMethodException) {
            null
        }
        method?.let {
            it.isAccessible = true
            return it.invoke(instance, *args)
        }
        return null
    }

    private fun getMethod(name: String, clazz: Class<*>, paramsClass: Array<Class<*>>): Method {
        var cls = clazz?: null
        while (cls != null) {
            val method = try {
                cls.getDeclaredMethod(name, *paramsClass)
            }catch (e: NoSuchMethodException) {
                null
            }
            if (method != null) {
                method.isAccessible = true
                return method
            }

            cls = cls.superclass
        }
        throw NoSuchMethodException("${clazz::class.java} not found method $name")
    }
}