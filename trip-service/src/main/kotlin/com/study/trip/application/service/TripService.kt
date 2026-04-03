package com.study.trip.application.service

import com.study.common.event.TripCreatedEvent
import com.study.common.exception.EntityNotFoundException
import com.study.common.outbox.OutboxEventPublisher
import com.study.trip.application.dto.CreateTripRequest
import com.study.trip.application.dto.TripResponse
import com.study.trip.domain.entity.Trip
import com.study.trip.domain.repository.TripQueryRepository
import com.study.trip.domain.repository.TripRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TripService(
    private val tripRepository: TripRepository,
    private val tripQueryRepository: TripQueryRepository,
    private val outboxEventPublisher: OutboxEventPublisher
) {
    @Transactional
    fun createTrip(userId: Long, request: CreateTripRequest): TripResponse {
        val startDate = requireNotNull(request.startDate) { "Start date is required" }
        val endDate = requireNotNull(request.endDate) { "End date is required" }
        val trip = Trip(
            userId = userId, title = request.title, description = request.description,
            startDate = startDate, endDate = endDate
        )
        val saved = tripRepository.save(trip)
        outboxEventPublisher.publish("trip-events", TripCreatedEvent(saved.id, saved.userId, saved.title))
        return TripResponse.from(saved)
    }

    fun getTrip(id: Long): TripResponse {
        val trip = tripRepository.findById(id).orElseThrow { EntityNotFoundException("Trip", id) }
        return TripResponse.from(trip)
    }

    fun getTripsByUser(userId: Long): List<TripResponse> =
        tripRepository.findByUserId(userId).map { TripResponse.from(it) }

    fun searchTrips(
        userId: Long?, status: com.study.trip.domain.entity.TripStatus?,
        keyword: String?, startDateFrom: java.time.LocalDate?, startDateTo: java.time.LocalDate?
    ): List<TripResponse> =
        tripQueryRepository.searchTrips(userId, status, keyword, startDateFrom, startDateTo)
            .map { TripResponse.from(it) }
}
