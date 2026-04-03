package com.study.user.application.dto

import jakarta.validation.constraints.NotBlank

data class OAuth2LoginRequest(
    @field:NotBlank(message = "Authorization code is required")
    val code: String = "",

    val redirectUri: String = "http://localhost:5173/oauth/callback"
)
