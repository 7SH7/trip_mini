package com.study.trip.domain.repository

import com.study.trip.domain.entity.TripPlace
import org.springframework.data.jpa.repository.JpaRepository

interface TripPlaceRepository : JpaRepository<TripPlace, Long> {
    fun findByTripIdOrderByCreatedAtDesc(tripId: Long): List<TripPlace>
}
