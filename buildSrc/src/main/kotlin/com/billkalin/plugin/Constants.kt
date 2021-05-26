package com.billkalin.plugin

object Constants {

    const val PATCH_PATH = "quickfix"

    const val PATCH_PACKAGE_NAME = "com.billkalin.quick.fix.patch"
    const val PATCH_CLASS_SUFFIX = "Patch"
    const val PATCH_ORIGIN_CLASS_FIELD_NAME = "originClass"
    const val PATCH_ASSIST_CLASS_SUFFIX = "Assist"
    const val PATCH_STATIC_SUPER_METHOD_PREFIX = "staticSuper"
    const val PATCH_SUPER_METHOD_PREFIX = "super"
    const val PATCH_PUBLIC_METHOD_PREFIX = "public"
    const val PATCH_GET_REAL_METHOD_NAME = "getRealParams"
    const val REFLECT_CLASS_NAME = "com.billkalin.android.hot.qucikfix.common.ReflectUtils"

    const val TYPE_OBJECT = 'L'
    const val TYPE_ARRAY = '['
    const val TYPE_END_PACKAGE = ';'
    const val TYPE_PRIMATE = "ZCBSIJFDV"

    val R_File = mutableSetOf<String>().apply {
        add("R\$array")
        add("R\$xml")
        add("R\$styleable")
        add("R\$style")
        add("R\$string")
        add("R\$raw")
        add("R\$menu")
        add("R\$layout")
        add("R\$integer")
        add("R\$id")
        add("R\$drawable")
        add("R\$dimen")
        add("R\$color")
        add("R\$bool")
        add("R\$attr")
        add("R\$anim")
    }

}