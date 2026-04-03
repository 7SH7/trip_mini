package com.study.trip.application.service

import com.study.common.event.DomainEvent
import com.study.common.event.TripCreatedEvent
import com.study.common.exception.EntityNotFoundException
import com.study.trip.application.dto.CreateTripRequest
import com.study.trip.application.dto.TripResponse
import com.study.trip.domain.entity.Trip
import com.study.trip.domain.repository.TripRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TripService(
    private val tripRepository: TripRepository,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>
) {
    @Transactional
    fun createTrip(userId: Long, request: CreateTripRequest): TripResponse {
        val trip = Trip(
            userId = userId, title = request.title, description = request.description,
            startDate = request.startDate!!, endDate = request.endDate!!
        )
        val saved = tripRepository.save(trip)
        kafkaTemplate.send("trip-events", TripCreatedEvent(saved.id, saved.userId, saved.title))
        return TripResponse.from(saved)
    }

    fun getTrip(id: Long): TripResponse {
        val trip = tripRepository.findById(id).orElseThrow { EntityNotFoundException("Trip", id) }
        return TripResponse.from(trip)
    }

    fun getTripsByUser(userId: Long): List<TripResponse> =
        tripRepository.findByUserId(userId).map { TripResponse.from(it) }
}
