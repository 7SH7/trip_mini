package com.study.trip.domain.repository

import com.study.trip.domain.entity.Trip
import org.springframework.data.jpa.repository.JpaRepository

interface TripRepository : JpaRepository<Trip, Long> {
    fun findByUserId(userId: Long): List<Trip>
}
