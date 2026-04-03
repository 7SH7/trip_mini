package com.study.user.application.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
