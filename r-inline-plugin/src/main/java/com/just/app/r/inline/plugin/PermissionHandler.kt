package com.just.app.r.inline.plugin

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class PermissionHandler : DefaultHandler() {

    val permissions = mutableSetOf<String>()

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
        super.startElement(uri, localName, qName, attributes)
        attributes ?: return
        when (localName) {
            "manifest" -> {

                println(
                    "PermissionHandler: localName = $localName, package = ${attributes.getValue(
                        "package"
                    )}"
                )
            }
            "application" -> {
                println(
                    "PermissionHandler: localName = $localName, package = ${attributes.getValue(
                        "android:name"
                    )}"
                )
            }
            "uses-permission" -> {
                println(
                    "PermissionHandler: localName = $localName, package = ${attributes.getValue(
                        "android:name"
                    )}"
                )
            }
        }
    }
}