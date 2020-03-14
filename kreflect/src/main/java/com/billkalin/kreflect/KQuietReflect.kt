package com.billkalin.kreflect

class KQuietReflect protected constructor() : com.billkalin.kreflect.KReflect() {
    var ignored: Throwable? = null
        protected set

    protected fun skip(): Boolean {
        return skipAlways() || ignored != null
    }

    protected fun skipAlways(): Boolean {
        return mType == null
    }

    override fun constructor(vararg parameterTypes: Class<*>?): KQuietReflect {
        if (skipAlways()) {
            return this
        }
        try {
            ignored = null
            super.constructor(*parameterTypes)
        } catch (e: Throwable) {
            ignored = e
        }
        return this
    }

    override fun <R> newInstance(vararg initargs: Any?): R? {
        if (skip()) {
            return null
        }
        try {
            ignored = null
            return super.newInstance(*initargs)
        } catch (e: Throwable) {
            ignored = e
        }
        return null
    }

    override fun bind(obj: Any?): KQuietReflect {
        if (skipAlways()) {
            return this
        }
        try {
            ignored = null
            super.bind(obj)
        } catch (e: Throwable) {
            ignored = e
        }
        return this
    }

    override fun unbind(): KQuietReflect {
        super.unbind()
        return this
    }

    override fun field(name: String): KQuietReflect {
        if (skipAlways()) {
            return this
        }
        try {
            ignored = null
            super.field(name)
        } catch (e: Throwable) {
            ignored = e
        }
        return this
    }

    override fun <R> get(): R? {
        if (skip()) {
            return null
        }
        try {
            ignored = null
            return super.get()
        } catch (e: Throwable) {
            ignored = e
        }
        return null
    }

    override fun <R> get(caller: Any?): R? {
        if (skip()) {
            return null
        }
        try {
            ignored = null
            return super.get(caller)
        } catch (e: Throwable) {
            ignored = e
        }
        return null
    }

    override fun set(value: Any?): KQuietReflect {
        if (skip()) {
            return this
        }
        try {
            ignored = null
            super.set(value)
        } catch (e: Throwable) {
            ignored = e
        }
        return this
    }

    override fun set(
        caller: Any?,
        value: Any?
    ): KQuietReflect {
        if (skip()) {
            return this
        }
        try {
            ignored = null
            super.set(caller, value)
        } catch (e: Throwable) {
            ignored = e
        }
        return this
    }

    override fun method(
        name: String,
        vararg parameterTypes: Class<*>?
    ): KQuietReflect {
        if (skipAlways()) {
            return this
        }
        try {
            ignored = null
            super.method(name, *parameterTypes)
        } catch (e: Throwable) {
            ignored = e
        }
        return this
    }

    override fun <R> call(vararg args: Any?): R? {
        if (skip()) {
            return null
        }
        try {
            ignored = null
            return super.call(*args)
        } catch (e: Throwable) {
            ignored = e
        }
        return null
    }

    override fun <R> callByCaller(
        caller: Any?,
        vararg args: Any?
    ): R? {
        if (skip()) {
            return null
        }
        try {
            ignored = null
            return super.callByCaller(caller, *args)
        } catch (e: Throwable) {
            ignored = e
        }
        return null
    }

    companion object {
        @JvmOverloads
        fun on(
            name: String,
            initialize: Boolean = true,
            loader: ClassLoader? = KQuietReflect::class.java.classLoader
        ): KQuietReflect {
            var cls: Class<*>? = null
            return try {
                cls = Class.forName(name, initialize, loader)
                on(cls, null)
            } catch (e: Throwable) {
                on(cls, e)
            }
        }

        fun on(type: Class<*>?): KQuietReflect {
            return on(
                type,
                if (type == null) KReflectException("Type was null!") else null
            )
        }

        private fun on(
            type: Class<*>?,
            ignored: Throwable?
        ): KQuietReflect {
            val reflector = KQuietReflect()
            reflector.mType = type
            reflector.ignored = ignored
            return reflector
        }

        fun with(caller: Any?): KQuietReflect {
            return if (caller == null) {
                on(null as Class<*>?)
            } else on(caller.javaClass)
                .bind(caller)
        }
    }
}