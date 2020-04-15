package com.just.app.permission.check.plugin

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
            "uses-permission" -> {
                permissions.add(attributes.getValue("android:name"))
            }
        }
    }
}