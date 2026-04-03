package com.study.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class InternalAuthFilter extends OncePerRequestFilter {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";

    private final InternalTokenProvider tokenProvider;

    public InternalAuthFilter(InternalTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String internalToken = request.getHeader(INTERNAL_TOKEN_HEADER);

        if (internalToken != null) {
            InternalTokenProvider.TokenPayload payload = tokenProvider.validateToken(internalToken);
            if (payload != null) {
                request.setAttribute("internalService", payload.serviceName());
                // Wrap request to add the internal service header for downstream use
                filterChain.doFilter(new InternalServiceRequestWrapper(request, payload.serviceName()), response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
