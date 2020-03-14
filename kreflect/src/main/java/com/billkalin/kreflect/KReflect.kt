package com.billkalin.kreflect

import java.lang.reflect.*

open class KReflect protected constructor() {

    companion object {
        @JvmOverloads
        @Throws(KReflectException::class)
        fun on(
            name: String,
            initialize: Boolean = true,
            loader: ClassLoader? = KReflect::class.java.classLoader
        ): KReflect {
            return try {
                on(
                    Class.forName(
                        name,
                        initialize,
                        loader
                    )
                )
            } catch (e: Throwable) {
                throw KReflectException(
                    "Oops!",
                    e
                )
            }
        }

        fun on(type: Class<*>): KReflect {
            val reflector = KReflect()
            reflector.mType = type
            return reflector
        }

        @Throws(KReflectException::class)
        fun with(caller: Any): KReflect {
            return on(caller.javaClass)
                .bind(caller)
        }
    }

    protected var mType: Class<*>? = null
    protected var mCaller: Any? = null
    protected var mConstructor: Constructor<*>? = null
    protected var mField: Field? = null
    protected var mMethod: Method? = null

    @Throws(KReflectException::class)
    open fun constructor(vararg parameterTypes: Class<*>?): KReflect {
        return try {
            mConstructor = mType!!.getDeclaredConstructor(*parameterTypes).apply {
                isAccessible = true
            }
            mField = null
            mMethod = null
            this
        } catch (e: Throwable) {
            throw KReflectException("Oops!", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(KReflectException::class)
    open fun <R> newInstance(vararg initargs: Any?): R? {
        if (mConstructor == null) {
            throw KReflectException("Constructor was null!")
        }
        val constructor = mConstructor
        return try {
            constructor?.newInstance(*initargs) as R?
        } catch (e: InvocationTargetException) {
            throw KReflectException(
                "Oops!",
                e.targetException
            )
        } catch (e: Throwable) {
            throw KReflectException("Oops!", e)
        }
    }

    @Throws(KReflectException::class)
    protected fun checked(caller: Any?): Any? {
        if (caller == null || mType!!.isInstance(caller)) {
            return caller
        }
        throw KReflectException("Caller [$caller] is not a instance of type [$mType]!")
    }

    @Throws(KReflectException::class)
    protected fun check(
        caller: Any?,
        member: Member?,
        name: String
    ) {
        if (member == null) {
            throw KReflectException("$name was null!")
        }
        if (caller == null && !Modifier.isStatic(member.modifiers)) {
            throw KReflectException("Need a caller!")
        }
        checked(caller)
    }

    @Throws(KReflectException::class)
    open fun bind(caller: Any?): KReflect {
        mCaller = checked(caller)
        return this
    }

    open fun unbind(): KReflect {
        mCaller = null
        return this
    }

    @Throws(KReflectException::class)
    open fun field(name: String): KReflect {
        return try {
            mField = findField(name)
            mField!!.isAccessible = true
            mConstructor = null
            mMethod = null
            this
        } catch (e: Throwable) {
            throw KReflectException("Oops!", e)
        }
    }

    @Throws(NoSuchFieldException::class)
    protected fun findField(name: String): Field {
        return try {
            mType!!.getField(name)
        } catch (e: NoSuchFieldException) {
            var cls = mType
            while (cls != null) {
                try {
                    return cls.getDeclaredField(name)
                } catch (ex: NoSuchFieldException) {
                    // Ignored
                }
                cls = cls.superclass
            }
            throw e
        }
    }

    @Throws(KReflectException::class)
    open fun <R> get(): R? {
        return get(mCaller)
    }

    @Throws(KReflectException::class)
    open fun <R> get(caller: Any?): R? {
        check(caller, mField, "Field")
        return try {
            mField!![caller] as R
        } catch (e: Throwable) {
            throw KReflectException("Oops!", e)
        }
    }

    @Throws(KReflectException::class)
    open fun set(value: Any?): KReflect {
        return set(mCaller, value)
    }

    @Throws(KReflectException::class)
    open operator fun set(caller: Any?, value: Any?): KReflect {
        check(caller, mField, "Field")
        return try {
            mField!![caller] = value
            this
        } catch (e: Throwable) {
            throw KReflectException("Oops!", e)
        }
    }

    @Throws(KReflectException::class)
    open fun method(
        name: String,
        vararg parameterTypes: Class<*>?
    ): KReflect {
        return try {
            mMethod = findMethod(name, *parameterTypes)
            mMethod!!.isAccessible = true
            mConstructor = null
            mField = null
            this
        } catch (e: NoSuchMethodException) {
            throw KReflectException("Oops!", e)
        }
    }

    @Throws(NoSuchMethodException::class)
    protected fun findMethod(
        name: String,
        vararg parameterTypes: Class<*>?
    ): Method {
        return try {
            mType!!.getMethod(name, *parameterTypes)
        } catch (e: NoSuchMethodException) {
            var cls = mType
            while (cls != null) {
                try {
                    return cls.getDeclaredMethod(name, *parameterTypes)
                } catch (ex: NoSuchMethodException) {
                    // Ignored
                }
                cls = cls.superclass
            }
            throw e
        }
    }

    @Throws(KReflectException::class)
    open fun <R> call(vararg args: Any?): R? {
        return callByCaller(mCaller, *args)
    }

    @Throws(KReflectException::class)
    open fun <R> callByCaller(caller: Any?, vararg args: Any?): R? {
        check(caller, mMethod, "Method")
        return try {
            mMethod!!.invoke(caller, *args) as R
        } catch (e: InvocationTargetException) {
            throw KReflectException(
                "Oops!",
                e.targetException
            )
        } catch (e: Throwable) {
            throw KReflectException("Oops!", e)
        }
    }
}