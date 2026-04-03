package com.study.common.security

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Base64

object Aes256Encryptor {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12

    fun encrypt(plainText: String, secretKey: String): String {
        val keySpec = SecretKeySpec(Base64.getDecoder().decode(secretKey), "AES")
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = ByteArray(IV_LENGTH + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH)
        System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(cipherText: String, secretKey: String): String {
        val keySpec = SecretKeySpec(Base64.getDecoder().decode(secretKey), "AES")
        val decoded = Base64.getDecoder().decode(cipherText)

        val iv = decoded.copyOfRange(0, IV_LENGTH)
        val encrypted = decoded.copyOfRange(IV_LENGTH, decoded.size)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
}
