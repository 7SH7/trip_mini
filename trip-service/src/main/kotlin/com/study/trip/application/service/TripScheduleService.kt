package com.study.trip.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.trip.application.dto.CreateScheduleRequest
import com.study.trip.application.dto.TripScheduleResponse
import com.study.trip.application.dto.UpdateScheduleRequest
import com.study.trip.domain.entity.TripSchedule
import com.study.trip.domain.repository.TripScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TripScheduleService(
    private val scheduleRepository: TripScheduleRepository,
    private val memberService: TripMemberService
) {
    fun getSchedules(tripId: Long): List<TripScheduleResponse> =
        scheduleRepository.findByTripIdOrderByDateAscOrderIndexAsc(tripId).map { TripScheduleResponse.from(it) }

    @Transactional
    fun create(tripId: Long, userId: Long, request: CreateScheduleRequest): TripScheduleResponse {
        memberService.isMember(tripId, userId).also { if (!it) throw com.study.common.exception.InvalidRequestException("여행 멤버가 아닙니다.") }
        val schedule = scheduleRepository.save(TripSchedule(
            tripId = tripId,
            date = requireNotNull(request.date),
            title = request.title,
            memo = request.memo,
            startTime = request.startTime,
            endTime = request.endTime,
            orderIndex = request.orderIndex
        ))
        return TripScheduleResponse.from(schedule)
    }

    @Transactional
    fun update(tripId: Long, scheduleId: Long, userId: Long, request: UpdateScheduleRequest): TripScheduleResponse {
        memberService.isMember(tripId, userId).also { if (!it) throw com.study.common.exception.InvalidRequestException("여행 멤버가 아닙니다.") }
        val schedule = scheduleRepository.findById(scheduleId).orElseThrow { EntityNotFoundException("TripSchedule", scheduleId) }
        schedule.update(request.title, request.memo, requireNotNull(request.date), request.startTime, request.endTime, request.orderIndex)
        return TripScheduleResponse.from(schedule)
    }

    @Transactional
    fun delete(tripId: Long, scheduleId: Long, userId: Long) {
        memberService.isMember(tripId, userId).also { if (!it) throw com.study.common.exception.InvalidRequestException("여행 멤버가 아닙니다.") }
        scheduleRepository.deleteById(scheduleId)
    }
}
