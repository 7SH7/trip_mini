package com.study.user.interfaces.controller;

import com.study.common.dto.ApiResponse;
import com.study.user.application.dto.LoginRequest;
import com.study.user.application.dto.OAuth2LoginRequest;
import com.study.user.application.dto.RegisterRequest;
import com.study.user.application.dto.TokenResponse;
import com.study.user.application.service.AuthService;
import com.study.user.application.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oAuth2Service;

    @PostMapping("/register")
    public ApiResponse<TokenResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.created(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody String refreshToken) {
        return ApiResponse.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/google")
    public ApiResponse<TokenResponse> googleLogin(@RequestBody OAuth2LoginRequest request) {
        return ApiResponse.ok(oAuth2Service.loginWithGoogle(request.getAccessToken()));
    }

    @PostMapping("/kakao")
    public ApiResponse<TokenResponse> kakaoLogin(@RequestBody OAuth2LoginRequest request) {
        return ApiResponse.ok(oAuth2Service.loginWithKakao(request.getAccessToken()));
    }
}
