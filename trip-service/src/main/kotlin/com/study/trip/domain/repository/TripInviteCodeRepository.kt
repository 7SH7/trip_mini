package com.study.trip.domain.repository

import com.study.trip.domain.entity.TripInviteCode
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface TripInviteCodeRepository : JpaRepository<TripInviteCode, Long> {
    fun findByCode(code: String): Optional<TripInviteCode>
    fun findByTripId(tripId: Long): List<TripInviteCode>
}
