package com.study.common.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.util.*

class InternalServiceRequestWrapper(
    request: HttpServletRequest,
    serviceName: String
) : HttpServletRequestWrapper(request) {

    private val customHeaders = mutableMapOf("X-Internal-Service" to serviceName)

    override fun getHeader(name: String): String? =
        customHeaders[name] ?: super.getHeader(name)

    override fun getHeaderNames(): Enumeration<String> {
        val names = linkedSetOf<String>()
        names.addAll(customHeaders.keys)
        val original = super.getHeaderNames()
        while (original.hasMoreElements()) {
            names.add(original.nextElement())
        }
        return Collections.enumeration(names)
    }

    override fun getHeaders(name: String): Enumeration<String> {
        val value = customHeaders[name]
        if (value != null) return Collections.enumeration(listOf(value))
        return super.getHeaders(name)
    }
}
