package com.study.trip.domain.repository

import com.study.trip.domain.entity.JoinRequestStatus
import com.study.trip.domain.entity.TripJoinRequest
import org.springframework.data.jpa.repository.JpaRepository

interface TripJoinRequestRepository : JpaRepository<TripJoinRequest, Long> {
    fun findByTripIdAndStatus(tripId: Long, status: JoinRequestStatus): List<TripJoinRequest>
    fun findByUserIdAndStatus(userId: Long, status: JoinRequestStatus): List<TripJoinRequest>
    fun existsByTripIdAndUserIdAndStatus(tripId: Long, userId: Long, status: JoinRequestStatus): Boolean
}
