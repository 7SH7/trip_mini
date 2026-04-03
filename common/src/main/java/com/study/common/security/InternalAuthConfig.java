package com.study.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalAuthConfig {

    @Bean
    public InternalTokenProvider internalTokenProvider(
            @Value("${internal.auth.secret}") String secret) {
        return new InternalTokenProvider(secret);
    }
}
