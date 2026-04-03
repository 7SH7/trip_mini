package com.study.user.infrastructure.security

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TokenRedisService(
    private val redisTemplate: StringRedisTemplate
) {
    fun saveRefreshToken(userId: Long, refreshToken: String, expirationMs: Long) {
        redisTemplate.opsForValue().set("refresh:$userId", refreshToken, expirationMs, TimeUnit.MILLISECONDS)
    }

    fun getRefreshToken(userId: Long): String? =
        redisTemplate.opsForValue().get("refresh:$userId")

    fun deleteRefreshToken(userId: Long) {
        redisTemplate.delete("refresh:$userId")
    }

    fun blacklistAccessToken(accessToken: String, remainingMs: Long) {
        if (remainingMs > 0) {
            redisTemplate.opsForValue().set("blacklist:$accessToken", "blacklisted", remainingMs, TimeUnit.MILLISECONDS)
        }
    }

    fun isBlacklisted(accessToken: String): Boolean =
        redisTemplate.hasKey("blacklist:$accessToken") == true
}
