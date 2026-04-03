package com.study.subscription.infrastructure.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

@Component
class PortOneClient(
    @Value("\${portone.api-secret}") private val apiSecret: String,
    @Value("\${portone.store-id}") private val storeId: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val webClient = WebClient.builder()
        .baseUrl("https://api.portone.io")
        .defaultHeader("Authorization", "PortOne $apiSecret")
        .build()

    fun verifyPayment(paymentId: String, expectedAmount: BigDecimal): Boolean {
        return try {
            val response = webClient.get()
                .uri("/payments/$paymentId")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            val amount = response?.get("amount") as? Map<*, *>
            val paid = (amount?.get("total") as? Number)?.toLong()
            val status = response?.get("status") as? String

            status == "PAID" && paid == expectedAmount.toLong()
        } catch (e: Exception) {
            log.error("PortOne payment verification failed for {}", paymentId, e)
            false
        }
    }
}
