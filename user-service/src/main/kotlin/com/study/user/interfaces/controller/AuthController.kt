package com.study.user.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.user.application.dto.*
import com.study.user.application.service.AuthService
import com.study.user.application.service.OAuth2Service
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "인증", description = "로그인, 회원가입, OAuth2, 토큰 관리")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val oAuth2Service: OAuth2Service
) {
    @Operation(summary = "이메일 회원가입")
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ApiResponse<TokenResponse> =
        ApiResponse.created(authService.register(request))

    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(authService.login(request))

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshToken: String): ApiResponse<TokenResponse> =
        ApiResponse.ok(authService.refresh(refreshToken))

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authHeader: String,
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<Unit?> {
        authService.logout(authHeader.substring(7), userId.toLong())
        return ApiResponse(200, "OK", null)
    }

    @Operation(summary = "Google OAuth2 로그인")
    @PostMapping("/google")
    fun googleLogin(@Valid @RequestBody request: OAuth2LoginRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(oAuth2Service.loginWithGoogle(request.code, request.redirectUri))

    @Operation(summary = "Kakao OAuth2 로그인")
    @PostMapping("/kakao")
    fun kakaoLogin(@Valid @RequestBody request: OAuth2LoginRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(oAuth2Service.loginWithKakao(request.code, request.redirectUri))
}
