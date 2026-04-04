package com.study.trip.domain.repository

import com.study.trip.domain.entity.TripSchedule
import org.springframework.data.jpa.repository.JpaRepository

interface TripScheduleRepository : JpaRepository<TripSchedule, Long> {
    fun findByTripIdOrderByDateAscOrderIndexAsc(tripId: Long): List<TripSchedule>
}
