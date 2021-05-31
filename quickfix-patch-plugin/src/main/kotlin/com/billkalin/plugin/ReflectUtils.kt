package com.billkalin.plugin

import java.lang.reflect.Field

object ReflectUtils {

    @JvmStatic
    fun getFieldValue(instance: Any, fieldName: String): Any? {
        var clazz = instance::class.java ?: null
        var field: Field? = null
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName)
                if (field != null){
                    field.isAccessible = true
                    return field.get(instance)
                }
            } catch (e: NoSuchFieldException) {

            }
            clazz = clazz.superclass
        }
        throw NoSuchFieldException("${instance::class.java} not found field $fieldName")
    }

}