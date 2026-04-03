package com.study.user.interfaces.controller;

import com.study.common.dto.ApiResponse;
import com.study.user.application.dto.LoginRequest;
import com.study.user.application.dto.RegisterRequest;
import com.study.user.application.dto.TokenResponse;
import com.study.user.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}
