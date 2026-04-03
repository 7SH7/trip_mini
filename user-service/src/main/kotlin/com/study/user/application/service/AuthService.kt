package com.study.user.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.common.exception.InvalidRequestException
import com.study.user.application.dto.LoginRequest
import com.study.user.application.dto.RegisterRequest
import com.study.user.application.dto.TokenResponse
import com.study.user.domain.entity.AuthProvider
import com.study.user.domain.entity.Role
import com.study.user.domain.entity.User
import com.study.user.domain.repository.UserRepository
import com.study.user.infrastructure.security.JwtTokenProvider
import com.study.user.infrastructure.security.TokenRedisService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenRedisService: TokenRedisService
) {
    @Transactional
    fun register(request: RegisterRequest): TokenResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw InvalidRequestException("Email already exists: ${request.email}")
        }
        val user = User(
            email = request.email,
            name = request.name,
            password = passwordEncoder.encode(request.password),
            role = Role.USER,
            provider = AuthProvider.LOCAL
        )
        val saved = userRepository.save(user)
        return issueAndStoreTokens(saved)
    }

    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { InvalidRequestException("Invalid email or password") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidRequestException("Invalid email or password")
        }
        return issueAndStoreTokens(user)
    }

    fun refresh(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw InvalidRequestException("Invalid refresh token")
        }
        val userId = jwtTokenProvider.getUserId(refreshToken)
        val storedToken = tokenRedisService.getRefreshToken(userId)
        if (storedToken == null || storedToken != refreshToken) {
            throw InvalidRequestException("Refresh token not found or revoked")
        }
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User", userId) }
        return issueAndStoreTokens(user)
    }

    fun logout(accessToken: String, userId: Long) {
        val claims = jwtTokenProvider.parseToken(accessToken)
        val remainingMs = claims.expiration.time - System.currentTimeMillis()
        tokenRedisService.blacklistAccessToken(accessToken, remainingMs)
        tokenRedisService.deleteRefreshToken(userId)
    }

    fun issueAndStoreTokens(user: User): TokenResponse {
        val accessToken = jwtTokenProvider.createAccessToken(user.id, user.email, user.role.name)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id)
        tokenRedisService.saveRefreshToken(user.id, refreshToken, 604800000L)
        return TokenResponse(accessToken, refreshToken)
    }
}
