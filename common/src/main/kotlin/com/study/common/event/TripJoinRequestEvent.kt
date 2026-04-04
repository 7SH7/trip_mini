package com.study.common.event

data class TripJoinRequestedEvent(
    val requestId: Long,
    val tripId: Long,
    val tripTitle: String,
    val requestUserId: Long,
    val ownerUserId: Long
) : DomainEvent()

data class TripJoinApprovedEvent(
    val requestId: Long,
    val tripId: Long,
    val tripTitle: String,
    val requestUserId: Long,
    val approvedBy: Long
) : DomainEvent()

data class TripJoinRejectedEvent(
    val requestId: Long,
    val tripId: Long,
    val tripTitle: String,
    val requestUserId: Long,
    val rejectedBy: Long
) : DomainEvent()
