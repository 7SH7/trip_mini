package com.study.common.security

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class EncryptedStringConverter : AttributeConverter<String?, String?> {

    companion object {
        // In production, load from environment variable
        var SECRET_KEY: String = System.getenv("AES_SECRET_KEY")
            ?: "dHJpcC1hZXMyNTYtc2VjcmV0LWtleS0zMi1ieXRlcw=="  // 32 bytes base64
    }

    override fun convertToDatabaseColumn(attribute: String?): String? {
        if (attribute.isNullOrBlank()) return attribute
        return try {
            Aes256Encryptor.encrypt(attribute, SECRET_KEY)
        } catch (e: Exception) {
            attribute
        }
    }

    override fun convertToEntityAttribute(dbData: String?): String? {
        if (dbData.isNullOrBlank()) return dbData
        return try {
            Aes256Encryptor.decrypt(dbData, SECRET_KEY)
        } catch (e: Exception) {
            dbData
        }
    }
}
