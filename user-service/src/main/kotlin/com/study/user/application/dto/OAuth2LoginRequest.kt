package com.study.user.application.dto

import jakarta.validation.constraints.NotBlank

data class OAuth2LoginRequest(
    @field:NotBlank(message = "Access token is required")
    val accessToken: String = ""
)
