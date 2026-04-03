package com.study.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class InternalAuthFilter(
    private val tokenProvider: InternalTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val internalToken = request.getHeader(INTERNAL_TOKEN_HEADER)

        if (internalToken != null) {
            val payload = tokenProvider.validateToken(internalToken)
            if (payload != null) {
                request.setAttribute("internalService", payload.serviceName)
                filterChain.doFilter(
                    InternalServiceRequestWrapper(request, payload.serviceName),
                    response
                )
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private const val INTERNAL_TOKEN_HEADER = "X-Internal-Token"
    }
}
