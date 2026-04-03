package com.study.user.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-token-validity}") private val accessTokenValidity: Long,
    @Value("\${jwt.refresh-token-validity}") private val refreshTokenValidity: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))

    fun createAccessToken(userId: Long, email: String, role: String): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(Date(now.time + accessTokenValidity))
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(userId: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + refreshTokenValidity))
            .signWith(key)
            .compact()
    }

    fun parseToken(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    fun validateToken(token: String): Boolean = try {
        parseToken(token); true
    } catch (e: JwtException) { false }
    catch (e: IllegalArgumentException) { false }

    fun getUserId(token: String): Long = parseToken(token).subject.toLong()
    fun getEmail(token: String): String = parseToken(token).get("email", String::class.java)
}
