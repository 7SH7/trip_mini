package com.study.accommodation.application.service

import com.study.accommodation.application.dto.AccommodationResponse
import com.study.accommodation.domain.entity.Accommodation
import com.study.accommodation.domain.repository.AccommodationRepository
import com.study.accommodation.infrastructure.external.TourApiClient
import com.study.common.exception.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AccommodationService(
    private val accommodationRepository: AccommodationRepository,
    private val tourApiClient: TourApiClient,
    private val priceParser: PriceParser
) {
    @Transactional
    fun search(keyword: String?, areaCode: String?, page: Int, size: Int): Page<AccommodationResponse> {
        val cachedResults = if (!keyword.isNullOrBlank()) {
            accommodationRepository.findByTitleContaining(keyword, PageRequest.of(page, size))
        } else if (!areaCode.isNullOrBlank()) {
            accommodationRepository.findByAreaCode(areaCode, PageRequest.of(page, size))
        } else {
            accommodationRepository.findAll(PageRequest.of(page, size))
        }

        if (cachedResults.totalElements > 0) {
            return cachedResults.map { AccommodationResponse.from(it) }
        }

        val apiResult = tourApiClient.searchStay(areaCode, keyword, page + 1, size)
        val saved = apiResult.items.map { item ->
            val existing = accommodationRepository.findByContentId(item.contentId)
            if (existing.isPresent) {
                existing.get()
            } else {
                accommodationRepository.save(
                    Accommodation(
                        contentId = item.contentId,
                        title = item.title,
                        address = item.addr1,
                        areaCode = item.areaCode,
                        sigunguCode = item.sigunguCode,
                        latitude = item.mapY,
                        longitude = item.mapX,
                        imageUrl = item.firstImage,
                        tel = item.tel,
                        category = item.cat3
                    )
                )
            }
        }
        return PageImpl(
            saved.map { AccommodationResponse.from(it) },
            PageRequest.of(page, size),
            apiResult.totalCount.toLong()
        )
    }

    fun getById(id: Long): AccommodationResponse {
        val accommodation = accommodationRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Accommodation", id) }
        return AccommodationResponse.from(accommodation)
    }
}
