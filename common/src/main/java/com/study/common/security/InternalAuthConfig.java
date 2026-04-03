package com.study.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalAuthConfig {

    @Bean
    public InternalTokenProvider internalTokenProvider(
            @Value("${internal.auth.secret}") String secret) {
        return new InternalTokenProvider(secret);
    }

    @Bean
    public FilterRegistrationBean<InternalAuthFilter> internalAuthFilter(InternalTokenProvider tokenProvider) {
        FilterRegistrationBean<InternalAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new InternalAuthFilter(tokenProvider));
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
