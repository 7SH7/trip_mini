package com.study.trip.domain.repository

import com.study.trip.domain.entity.TripMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TripMemberRepository : JpaRepository<TripMember, Long> {
    fun findByTripId(tripId: Long): List<TripMember>
    fun findByTripIdAndUserId(tripId: Long, userId: Long): TripMember?
    fun existsByTripIdAndUserId(tripId: Long, userId: Long): Boolean

    @Query("SELECT m.tripId FROM TripMember m WHERE m.userId = :userId")
    fun findTripIdsByUserId(userId: Long): List<Long>
}
