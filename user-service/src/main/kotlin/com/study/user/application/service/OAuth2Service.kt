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
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

@Service
class OAuth2Service(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenRedisService: TokenRedisService,
    @Qualifier("oAuth2RestClient") private val restClient: RestClient,
    @Value("\${oauth2.google.client-id}") private val googleClientId: String,
    @Value("\${oauth2.google.client-secret}") private val googleClientSecret: String,
    @Value("\${oauth2.kakao.client-id}") private val kakaoClientId: String,
    @Value("\${oauth2.kakao.client-secret}") private val kakaoClientSecret: String
) {

    // ===== Google =====

    @Transactional
    fun loginWithGoogle(code: String, redirectUri: String): TokenResponse {
        val accessToken = exchangeGoogleCode(code, redirectUri)
        val userInfo = getGoogleUserInfo(accessToken)

        val email = userInfo["email"] as String
        val name = userInfo["name"] as String
        val providerId = userInfo["sub"] as String

        val user = findOrCreateOAuth2User(email, name, AuthProvider.GOOGLE, providerId)
        return issueAndStoreTokens(user)
    }

    private fun exchangeGoogleCode(code: String, redirectUri: String): String {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("code", code)
            add("client_id", googleClientId)
            add("client_secret", googleClientSecret)
            add("redirect_uri", redirectUri)
            add("grant_type", "authorization_code")
        }

        val response = restClient.post()
            .uri("https://oauth2.googleapis.com/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(params)
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
            ?: throw InvalidRequestException("Failed to exchange Google authorization code")

        return response["access_token"] as? String
            ?: throw InvalidRequestException("Google token response missing access_token")
    }

    private fun getGoogleUserInfo(accessToken: String): Map<String, Any> {
        return restClient.get()
            .uri("https://www.googleapis.com/oauth2/v3/userinfo")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
            ?: throw InvalidRequestException("Failed to fetch Google user info")
    }

    // ===== Kakao =====

    @Transactional
    @Suppress("UNCHECKED_CAST")
    fun loginWithKakao(code: String, redirectUri: String): TokenResponse {
        val accessToken = exchangeKakaoCode(code, redirectUri)
        val userInfo = getKakaoUserInfo(accessToken)

        val kakaoAccount = userInfo["kakao_account"] as Map<String, Any>
        val profile = kakaoAccount["profile"] as Map<String, Any>
        val email = kakaoAccount["email"] as String
        val name = profile["nickname"] as String
        val providerId = userInfo["id"].toString()

        val user = findOrCreateOAuth2User(email, name, AuthProvider.KAKAO, providerId)
        return issueAndStoreTokens(user)
    }

    private fun exchangeKakaoCode(code: String, redirectUri: String): String {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("code", code)
            add("client_id", kakaoClientId)
            add("client_secret", kakaoClientSecret)
            add("redirect_uri", redirectUri)
            add("grant_type", "authorization_code")
        }

        val response = restClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(params)
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
            ?: throw InvalidRequestException("Failed to exchange Kakao authorization code")

        return response["access_token"] as? String
            ?: throw InvalidRequestException("Kakao token response missing access_token")
    }

    private fun getKakaoUserInfo(accessToken: String): Map<String, Any> {
        return restClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
            ?: throw InvalidRequestException("Failed to fetch Kakao user info")
    }

    // ===== Common =====

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
