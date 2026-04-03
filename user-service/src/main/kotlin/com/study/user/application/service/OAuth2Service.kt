package com.study.user.application.service

import com.study.common.exception.InvalidRequestException
import com.study.user.application.dto.TokenResponse
import com.study.user.domain.entity.AuthProvider
import com.study.user.domain.entity.Role
import com.study.user.domain.entity.User
import com.study.user.domain.repository.UserRepository
import com.study.user.infrastructure.security.JwtTokenProvider
import com.study.user.infrastructure.security.TokenRedisService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient

@Service
class OAuth2Service(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenRedisService: TokenRedisService,
    @Qualifier("oAuth2RestClient") private val restClient: RestClient
) {
    @Transactional
    fun loginWithGoogle(accessToken: String): TokenResponse {
        val userInfo = restClient.get()
            .uri("https://www.googleapis.com/oauth2/v3/userinfo")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
            ?: throw InvalidRequestException("Failed to fetch user info from OAuth2 provider")

        val email = userInfo["email"] as String
        val name = userInfo["name"] as String
        val providerId = userInfo["sub"] as String

        val user = findOrCreateOAuth2User(email, name, AuthProvider.GOOGLE, providerId)
        return issueAndStoreTokens(user)
    }

    @Transactional
    @Suppress("UNCHECKED_CAST")
    fun loginWithKakao(accessToken: String): TokenResponse {
        val userInfo = restClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
            ?: throw InvalidRequestException("Failed to fetch user info from OAuth2 provider")

        val kakaoAccount = userInfo["kakao_account"] as Map<String, Any>
        val profile = kakaoAccount["profile"] as Map<String, Any>
        val email = kakaoAccount["email"] as String
        val name = profile["nickname"] as String
        val providerId = userInfo["id"].toString()

        val user = findOrCreateOAuth2User(email, name, AuthProvider.KAKAO, providerId)
        return issueAndStoreTokens(user)
    }

    private fun findOrCreateOAuth2User(
        email: String, name: String, provider: AuthProvider, providerId: String
    ): User {
        val existing = userRepository.findByEmail(email)
        return if (existing.isPresent) {
            existing.get().also { it.updateOAuth2Info(name, providerId) }
        } else {
            userRepository.save(User(
                email = email, name = name, role = Role.USER,
                provider = provider, providerId = providerId
            ))
        }
    }

    private fun issueAndStoreTokens(user: User): TokenResponse {
        val accessToken = jwtTokenProvider.createAccessToken(user.id, user.email, user.role.name)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id)
        tokenRedisService.saveRefreshToken(user.id, refreshToken, 604800000L)
        return TokenResponse(accessToken, refreshToken)
    }
}
