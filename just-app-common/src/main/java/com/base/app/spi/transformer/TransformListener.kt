package com.base.app.spi.transformer

interface TransformListener {
    fun onPreTransform(context: TransformContext) {}

    fun onPostTransform(context: TransformContext) {}
}