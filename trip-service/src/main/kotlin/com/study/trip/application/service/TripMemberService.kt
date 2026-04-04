package com.study.trip.application.service

import com.study.common.event.TripJoinApprovedEvent
import com.study.common.event.TripJoinRejectedEvent
import com.study.common.event.TripJoinRequestedEvent
import com.study.common.exception.EntityNotFoundException
import com.study.common.exception.InvalidRequestException
import com.study.common.outbox.OutboxEventPublisher
import com.study.trip.application.dto.*
import com.study.trip.domain.entity.*
import com.study.trip.domain.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class TripMemberService(
    private val tripRepository: TripRepository,
    private val tripMemberRepository: TripMemberRepository,
    private val inviteCodeRepository: TripInviteCodeRepository,
    private val joinRequestRepository: TripJoinRequestRepository,
    private val outboxEventPublisher: OutboxEventPublisher
) {
    companion object {
        const val MAX_MEMBERS = 5
    }

    @Transactional
    fun addOwner(tripId: Long, userId: Long) {
        tripMemberRepository.save(TripMember(tripId = tripId, userId = userId, role = MemberRole.OWNER))
    }

    fun getMembers(tripId: Long): List<TripMemberResponse> =
        tripMemberRepository.findByTripId(tripId).map { TripMemberResponse.from(it) }

    fun isMember(tripId: Long, userId: Long): Boolean =
        tripMemberRepository.existsByTripIdAndUserId(tripId, userId)

    @Transactional
    fun generateInviteCode(tripId: Long, userId: Long): InviteCodeResponse {
        requireMember(tripId, userId)
        val code = UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
        val invite = inviteCodeRepository.save(TripInviteCode(tripId = tripId, code = code))
        return InviteCodeResponse.from(invite)
    }

    // === Saga Step 1: 참여 요청 생성 (PENDING) ===
    @Transactional
    fun requestJoin(userId: Long, request: JoinTripRequest): TripJoinRequestResponse {
        val invite = inviteCodeRepository.findByCode(request.code)
            .orElseThrow { InvalidRequestException("유효하지 않은 초대 코드입니다.") }

        if (invite.isExpired()) throw InvalidRequestException("만료된 초대 코드입니다.")
        if (tripMemberRepository.existsByTripIdAndUserId(invite.tripId, userId))
            throw InvalidRequestException("이미 참여 중인 여행입니다.")
        if (joinRequestRepository.existsByTripIdAndUserIdAndStatus(invite.tripId, userId, JoinRequestStatus.PENDING))
            throw InvalidRequestException("이미 참여 요청을 보냈습니다.")

        val currentCount = tripMemberRepository.findByTripId(invite.tripId).size
        if (currentCount >= MAX_MEMBERS)
            throw InvalidRequestException("여행 인원이 최대 ${MAX_MEMBERS}명을 초과할 수 없습니다.")

        val trip = tripRepository.findById(invite.tripId)
            .orElseThrow { EntityNotFoundException("Trip", invite.tripId) }
        val owner = tripMemberRepository.findByTripId(invite.tripId)
            .firstOrNull { it.role == MemberRole.OWNER }
            ?: throw InvalidRequestException("여행 방장을 찾을 수 없습니다.")

        val joinRequest = joinRequestRepository.save(
            TripJoinRequest(tripId = invite.tripId, userId = userId, inviteCode = request.code)
        )

        // 방장에게 알림 이벤트 발행
        outboxEventPublisher.publish("trip-events",
            TripJoinRequestedEvent(joinRequest.id, trip.id, trip.title, userId, owner.userId))

        return TripJoinRequestResponse.from(joinRequest)
    }

    // === Saga Step 2a: 승인 (Confirm) ===
    @Transactional
    fun approveJoinRequest(tripId: Long, requestId: Long, approverUserId: Long): TripJoinRequestResponse {
        val approver = tripMemberRepository.findByTripIdAndUserId(tripId, approverUserId)
            ?: throw InvalidRequestException("여행 멤버가 아닙니다.")
        if (approver.role != MemberRole.OWNER)
            throw InvalidRequestException("방장만 참여 요청을 승인할 수 있습니다.")

        val joinRequest = joinRequestRepository.findById(requestId)
            .orElseThrow { EntityNotFoundException("TripJoinRequest", requestId) }
        if (joinRequest.status != JoinRequestStatus.PENDING)
            throw InvalidRequestException("이미 처리된 요청입니다.")

        // 인원 제한 체크
        val currentCount = tripMemberRepository.findByTripId(tripId).size
        if (currentCount >= MAX_MEMBERS)
            throw InvalidRequestException("여행 인원이 최대 ${MAX_MEMBERS}명을 초과할 수 없습니다.")

        // 승인 → 멤버 추가
        joinRequest.approve(approverUserId)
        tripMemberRepository.save(TripMember(tripId = tripId, userId = joinRequest.userId))

        val trip = tripRepository.findById(tripId)
            .orElseThrow { EntityNotFoundException("Trip", tripId) }

        // 요청자에게 승인 알림
        outboxEventPublisher.publish("trip-events",
            TripJoinApprovedEvent(joinRequest.id, tripId, trip.title, joinRequest.userId, approverUserId))

        return TripJoinRequestResponse.from(joinRequest)
    }

    // === Saga Step 2b: 거절 (Compensate) ===
    @Transactional
    fun rejectJoinRequest(tripId: Long, requestId: Long, rejecterUserId: Long): TripJoinRequestResponse {
        val rejecter = tripMemberRepository.findByTripIdAndUserId(tripId, rejecterUserId)
            ?: throw InvalidRequestException("여행 멤버가 아닙니다.")
        if (rejecter.role != MemberRole.OWNER)
            throw InvalidRequestException("방장만 참여 요청을 거절할 수 있습니다.")

        val joinRequest = joinRequestRepository.findById(requestId)
            .orElseThrow { EntityNotFoundException("TripJoinRequest", requestId) }
        if (joinRequest.status != JoinRequestStatus.PENDING)
            throw InvalidRequestException("이미 처리된 요청입니다.")

        // 거절 → 멤버 추가 안 함 (보상 트랜잭션)
        joinRequest.reject(rejecterUserId)

        val trip = tripRepository.findById(tripId)
            .orElseThrow { EntityNotFoundException("Trip", tripId) }

        // 요청자에게 거절 알림
        outboxEventPublisher.publish("trip-events",
            TripJoinRejectedEvent(joinRequest.id, tripId, trip.title, joinRequest.userId, rejecterUserId))

        return TripJoinRequestResponse.from(joinRequest)
    }

    fun getPendingRequests(tripId: Long): List<TripJoinRequestResponse> =
        joinRequestRepository.findByTripIdAndStatus(tripId, JoinRequestStatus.PENDING)
            .map { TripJoinRequestResponse.from(it) }

    fun getMyPendingRequests(userId: Long): List<TripJoinRequestResponse> =
        joinRequestRepository.findByUserIdAndStatus(userId, JoinRequestStatus.PENDING)
            .map { TripJoinRequestResponse.from(it) }

    @Transactional
    fun removeMember(tripId: Long, targetUserId: Long, requestUserId: Long) {
        val requester = tripMemberRepository.findByTripIdAndUserId(tripId, requestUserId)
            ?: throw InvalidRequestException("여행 멤버가 아닙니다.")
        if (requester.role != MemberRole.OWNER && requestUserId != targetUserId)
            throw InvalidRequestException("멤버를 내보낼 권한이 없습니다.")
        val target = tripMemberRepository.findByTripIdAndUserId(tripId, targetUserId)
            ?: throw EntityNotFoundException("TripMember", targetUserId)
        if (target.role == MemberRole.OWNER) throw InvalidRequestException("방장은 내보낼 수 없습니다.")
        tripMemberRepository.delete(target)
    }

    fun getMyTripsIncludingTeam(userId: Long): List<TripResponse> {
        val tripIds = tripMemberRepository.findTripIdsByUserId(userId)
        if (tripIds.isEmpty()) return emptyList()
        return tripRepository.findAllById(tripIds).map { TripResponse.from(it) }
    }

    private fun requireMember(tripId: Long, userId: Long) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId))
            throw InvalidRequestException("여행 멤버가 아닙니다.")
    }
}
