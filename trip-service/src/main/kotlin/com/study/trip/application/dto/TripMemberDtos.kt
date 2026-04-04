package com.study.trip.application.dto

import com.study.trip.domain.entity.MemberRole
import com.study.trip.domain.entity.TripInviteCode
import com.study.trip.domain.entity.TripMember
import java.time.LocalDateTime

data class TripMemberResponse(
    val id: Long,
    val tripId: Long,
    val userId: Long,
    val role: MemberRole,
    val joinedAt: LocalDateTime
) {
    companion object {
        fun from(m: TripMember) = TripMemberResponse(m.id, m.tripId, m.userId, m.role, m.joinedAt)
    }
}

data class InviteCodeResponse(
    val code: String,
    val tripId: Long,
    val expiresAt: LocalDateTime
) {
    companion object {
        fun from(c: TripInviteCode) = InviteCodeResponse(c.code, c.tripId, c.expiresAt)
    }
}

data class JoinTripRequest(val code: String = "")

data class TripJoinRequestResponse(
    val id: Long,
    val tripId: Long,
    val userId: Long,
    val inviteCode: String,
    val status: String,
    val createdAt: LocalDateTime,
    val processedAt: LocalDateTime?
) {
    companion object {
        fun from(r: com.study.trip.domain.entity.TripJoinRequest) = TripJoinRequestResponse(
            r.id, r.tripId, r.userId, r.inviteCode, r.status.name, r.createdAt, r.processedAt
        )
    }
}
