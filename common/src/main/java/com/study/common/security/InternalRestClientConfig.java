package com.study.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class InternalRestClientConfig {

    @Bean
    public RestClient internalRestClient(
            InternalTokenProvider tokenProvider,
            @Value("${spring.application.name}") String serviceName) {
        return RestClient.builder()
                .requestInitializer(request ->
                        request.getHeaders().set("X-Internal-Token", tokenProvider.generateToken(serviceName)))
                .build();
    }
}
