package com.study.common.security

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.BufferedReader

class XssRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    private val sanitizedBody: ByteArray? by lazy {
        val contentType = request.contentType ?: return@lazy null
        if (!contentType.contains("application/json") && !contentType.contains("text/")) return@lazy null

        val reader = request.inputStream.bufferedReader()
        val body = reader.readText()
        val sanitized = XssSanitizer.sanitize(body)
        sanitized?.toByteArray(Charsets.UTF_8)
    }

    override fun getParameter(name: String): String? {
        val value = super.getParameter(name)
        return XssSanitizer.sanitize(value)
    }

    override fun getParameterValues(name: String): Array<String>? {
        val values = super.getParameterValues(name) ?: return null
        return values.map { XssSanitizer.sanitize(it) ?: it }.toTypedArray()
    }

    override fun getHeader(name: String): String? {
        val value = super.getHeader(name)
        return XssSanitizer.sanitize(value)
    }

    override fun getInputStream(): ServletInputStream {
        val bytes = sanitizedBody ?: return super.getInputStream()
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        return object : ServletInputStream() {
            override fun read(): Int = byteArrayInputStream.read()
            override fun isFinished(): Boolean = byteArrayInputStream.available() == 0
            override fun isReady(): Boolean = true
            override fun setReadListener(listener: ReadListener?) {}
        }
    }

    override fun getReader(): BufferedReader {
        val bytes = sanitizedBody ?: return super.getReader()
        return BufferedReader(InputStreamReader(ByteArrayInputStream(bytes), Charsets.UTF_8))
    }
}
