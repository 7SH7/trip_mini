package com.study.common.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

class InternalTokenProvider(secretKey: String) {

    private val keySpec: SecretKeySpec

    init {
        val keyBytes = Base64.getDecoder().decode(secretKey)
        keySpec = SecretKeySpec(keyBytes, "AES")
    }

    fun generateToken(serviceName: String): String {
        val payload = "$serviceName:${Instant.now().epochSecond}"
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(payload.toByteArray())

        val buffer = ByteBuffer.allocate(IV_LENGTH + encrypted.size)
        buffer.put(iv)
        buffer.put(encrypted)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array())
    }

    fun validateToken(token: String): TokenPayload? {
        return try {
            val decoded = Base64.getUrlDecoder().decode(token)
            val buffer = ByteBuffer.wrap(decoded)
            val iv = ByteArray(IV_LENGTH).also { buffer.get(it) }
            val encrypted = ByteArray(buffer.remaining()).also { buffer.get(it) }

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val payload = String(cipher.doFinal(encrypted))

            val parts = payload.split(":")
            if (parts.size != 2) return null

            val serviceName = parts[0]
            val timestamp = parts[1].toLong()
            val now = Instant.now().epochSecond

            if (abs(now - timestamp) > TOKEN_VALIDITY_SECONDS) return null

            TokenPayload(serviceName, timestamp)
        } catch (e: Exception) {
            null
        }
    }

    data class TokenPayload(val serviceName: String, val timestamp: Long)

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_LENGTH = 12
        private const val TOKEN_VALIDITY_SECONDS = 30L
    }
}
