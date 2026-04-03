package com.study.common.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

@Configuration
class InternalRestClientConfig {

    @Bean
    @LoadBalanced
    fun loadBalancedRestTemplate(): RestTemplate = RestTemplate()

    @Bean
    fun internalRestClient(
        loadBalancedRestTemplate: RestTemplate,
        tokenProvider: InternalTokenProvider,
        @Value("\${spring.application.name}") serviceName: String
    ): RestClient = RestClient.builder(loadBalancedRestTemplate)
        .requestInitializer { request ->
            request.headers.set("X-Internal-Token", tokenProvider.generateToken(serviceName))
        }
        .build()
}
