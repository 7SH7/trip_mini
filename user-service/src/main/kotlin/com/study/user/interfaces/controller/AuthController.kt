package com.study.user.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.user.application.dto.*
import com.study.user.application.service.AuthService
import com.study.user.application.service.OAuth2Service
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val oAuth2Service: OAuth2Service
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ApiResponse<TokenResponse> =
        ApiResponse.created(authService.register(request))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(authService.login(request))

    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshToken: String): ApiResponse<TokenResponse> =
        ApiResponse.ok(authService.refresh(refreshToken))

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authHeader: String,
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<Unit?> {
        authService.logout(authHeader.substring(7), userId.toLong())
        return ApiResponse(200, "OK", null)
    }

    @PostMapping("/google")
    fun googleLogin(@Valid @RequestBody request: OAuth2LoginRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(oAuth2Service.loginWithGoogle(request.code, request.redirectUri))

    @PostMapping("/kakao")
    fun kakaoLogin(@Valid @RequestBody request: OAuth2LoginRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(oAuth2Service.loginWithKakao(request.code, request.redirectUri))
}
