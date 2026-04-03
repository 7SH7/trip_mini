package com.study.common.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InternalAuthConfig {

    @Bean
    fun internalTokenProvider(
        @Value("\${internal.auth.secret}") secret: String
    ): InternalTokenProvider = InternalTokenProvider(secret)

    @Bean
    fun internalAuthFilter(tokenProvider: InternalTokenProvider): FilterRegistrationBean<InternalAuthFilter> =
        FilterRegistrationBean(InternalAuthFilter(tokenProvider)).apply {
            addUrlPatterns("/api/*")
            order = 1
        }

    @Bean
    fun xssFilter(): FilterRegistrationBean<XssFilter> =
        FilterRegistrationBean(XssFilter()).apply {
            addUrlPatterns("/api/*")
            order = 0
        }

    @Bean
    fun securityHeaderFilter(): FilterRegistrationBean<SecurityHeaderFilter> =
        FilterRegistrationBean(SecurityHeaderFilter()).apply {
            addUrlPatterns("/*")
            order = -1
        }
}
