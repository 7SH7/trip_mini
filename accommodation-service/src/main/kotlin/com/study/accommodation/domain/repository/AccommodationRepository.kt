package com.study.accommodation.domain.repository

import com.study.accommodation.domain.entity.Accommodation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AccommodationRepository : JpaRepository<Accommodation, Long> {
    fun findByContentId(contentId: String): Optional<Accommodation>
    fun findByTitleContaining(keyword: String, pageable: Pageable): Page<Accommodation>
    fun findByAreaCode(areaCode: String, pageable: Pageable): Page<Accommodation>
}
