package com.study.accommodation.infrastructure.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class TourApiClient(
    @Value("\${tour-api.base-url}") private val baseUrl: String,
    @Value("\${tour-api.service-key}") private val serviceKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val webClient = WebClient.builder().baseUrl(baseUrl).build()

    fun searchStay(areaCode: String?, keyword: String?, page: Int, size: Int): TourApiResponse {
        return try {
            val uri = if (!keyword.isNullOrBlank()) {
                "/searchKeyword1?serviceKey=$serviceKey&keyword=$keyword&contentTypeId=32&MobileOS=ETC&MobileApp=Trip&_type=json&numOfRows=$size&pageNo=$page"
            } else {
                "/searchStay1?serviceKey=$serviceKey&areaCode=${areaCode ?: ""}&MobileOS=ETC&MobileApp=Trip&_type=json&numOfRows=$size&pageNo=$page"
            }

            val response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            parseResponse(response)
        } catch (e: Exception) {
            log.error("Tour API call failed", e)
            TourApiResponse(emptyList(), 0)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseResponse(response: Map<*, *>?): TourApiResponse {
        if (response == null) return TourApiResponse(emptyList(), 0)
        try {
            val body = (response["response"] as? Map<String, Any>)?.get("body") as? Map<String, Any>
                ?: return TourApiResponse(emptyList(), 0)
            val totalCount = (body["totalCount"] as? Number)?.toInt() ?: 0
            val items = (body["items"] as? Map<String, Any>)?.get("item") as? List<Map<String, Any>>
                ?: return TourApiResponse(emptyList(), totalCount)

            val stayItems = items.map { item ->
                TourApiStayItem(
                    contentId = item["contentid"]?.toString() ?: "",
                    title = item["title"]?.toString() ?: "",
                    addr1 = item["addr1"]?.toString(),
                    areaCode = item["areacode"]?.toString(),
                    sigunguCode = item["sigungucode"]?.toString(),
                    mapX = item["mapx"]?.toString()?.toDoubleOrNull(),
                    mapY = item["mapy"]?.toString()?.toDoubleOrNull(),
                    firstImage = item["firstimage"]?.toString(),
                    tel = item["tel"]?.toString(),
                    cat3 = item["cat3"]?.toString()
                )
            }
            return TourApiResponse(stayItems, totalCount)
        } catch (e: Exception) {
            log.error("Failed to parse Tour API response", e)
            return TourApiResponse(emptyList(), 0)
        }
    }
}

data class TourApiResponse(
    val items: List<TourApiStayItem>,
    val totalCount: Int
)

data class TourApiStayItem(
    val contentId: String,
    val title: String,
    val addr1: String?,
    val areaCode: String?,
    val sigunguCode: String?,
    val mapX: Double?,
    val mapY: Double?,
    val firstImage: String?,
    val tel: String?,
    val cat3: String?
)
