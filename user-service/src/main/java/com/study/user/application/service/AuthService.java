package com.study.user.application.service;

import com.study.user.application.dto.LoginRequest;
import com.study.user.application.dto.RegisterRequest;
import com.study.user.application.dto.TokenResponse;
import com.study.user.domain.entity.AuthProvider;
import com.study.user.domain.entity.Role;
import com.study.user.domain.entity.User;
import com.study.user.domain.repository.UserRepository;
import com.study.user.infrastructure.security.JwtTokenProvider;
import com.study.user.infrastructure.security.TokenRedisService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .build();
        User saved = userRepository.save(user);

        return issueAndStoreTokens(saved);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return issueAndStoreTokens(user);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        String storedToken = tokenRedisService.getRefreshToken(userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh token not found or revoked");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return issueAndStoreTokens(user);
    }

    public void logout(String accessToken, Long userId) {
        Claims claims = jwtTokenProvider.parseToken(accessToken);
        long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();
        tokenRedisService.blacklistAccessToken(accessToken, remainingMs);
        tokenRedisService.deleteRefreshToken(userId);
    }

    public TokenResponse issueAndStoreTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        tokenRedisService.saveRefreshToken(user.getId(), refreshToken, 604800000L);
        return new TokenResponse(accessToken, refreshToken);
    }
}
