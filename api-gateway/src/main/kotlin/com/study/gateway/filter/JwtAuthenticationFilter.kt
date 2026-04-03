package com.study.gateway.filter

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.SecretKey

@Component
class JwtAuthenticationFilter(
    @Value("\${jwt.secret}") secret: String,
    private val redisTemplate: ReactiveStringRedisTemplate
) : GlobalFilter, Ordered {

    private val key: SecretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))

    companion object {
        private val WHITELIST = listOf(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/google",
            "/api/auth/kakao"
        )
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path

        if (WHITELIST.any { path == it }) {
            return chain.filter(exchange)
        }

        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header")
        }

        val token = authHeader.substring(7)

        val claims = try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        } catch (e: Exception) {
            return onError(exchange, "Invalid or expired token")
        }

        return redisTemplate.hasKey("blacklist:$token")
            .flatMap { isBlacklisted ->
                if (isBlacklisted == true) {
                    onError(exchange, "Token has been revoked")
                } else {
                    val mutatedRequest = exchange.request.mutate()
                        .header("X-User-Id", claims.subject)
                        .header("X-User-Email", claims.get("email", String::class.java))
                        .header("X-User-Role", claims.get("role", String::class.java))
                        .build()
                    chain.filter(exchange.mutate().request(mutatedRequest).build())
                }
            }
    }

    private fun onError(exchange: ServerWebExchange, message: String): Mono<Void> {
        val response: ServerHttpResponse = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = MediaType.APPLICATION_JSON

        val body = """{"status":401,"message":"$message","data":null}"""
        val buffer: DataBuffer = response.bufferFactory().wrap(body.toByteArray(StandardCharsets.UTF_8))
        return response.writeWith(Mono.just(buffer))
    }

    override fun getOrder(): Int = -1
}
