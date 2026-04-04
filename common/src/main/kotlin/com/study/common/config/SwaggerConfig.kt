package com.study.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Trip MSA API")
                .description("여행 플랫폼 마이크로서비스 API 문서")
                .version("1.0.0")
                .contact(Contact().name("Trip Team"))
        )
        .addSecurityItem(SecurityRequirement().addList("X-User-Id"))
        .components(
            Components()
                .addSecuritySchemes(
                    "X-User-Id",
                    SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .`in`(SecurityScheme.In.HEADER)
                        .name("X-User-Id")
                        .description("API Gateway에서 JWT 인증 후 주입되는 사용자 ID")
                )
        )
}
