package com.study.accommodation.application.dto

import com.study.accommodation.domain.entity.Accommodation

data class AccommodationResponse(
    val id: Long,
    val contentId: String,
    val title: String,
    val address: String?,
    val imageUrl: String?,
    val tel: String?,
    val price: Int?,
    val priceRaw: String?,
    val latitude: Double?,
    val longitude: Double?,
    val category: String?
) {
    companion object {
        fun from(accommodation: Accommodation) = AccommodationResponse(
            id = accommodation.id,
            contentId = accommodation.contentId,
            title = accommodation.title,
            address = accommodation.address,
            imageUrl = accommodation.imageUrl,
            tel = accommodation.tel,
            price = accommodation.price,
            priceRaw = accommodation.priceRaw,
            latitude = accommodation.latitude,
            longitude = accommodation.longitude,
            category = accommodation.category
        )
    }
}
