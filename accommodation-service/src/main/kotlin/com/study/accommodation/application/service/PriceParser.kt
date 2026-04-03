package com.study.accommodation.application.service

import org.springframework.stereotype.Component

@Component
class PriceParser {

    private val pricePattern = Regex("([\\d,]+)\\s*원")

    fun parse(rawText: String?): Int? {
        if (rawText.isNullOrBlank()) return null
        val match = pricePattern.find(rawText) ?: return null
        return match.groupValues[1].replace(",", "").toIntOrNull()
    }
}
