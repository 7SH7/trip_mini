package com.study.booking.application.service

import com.study.booking.application.dto.CreateBookingRequest
import com.study.booking.application.dto.BookingResponse
import com.study.booking.domain.entity.Booking
import com.study.booking.domain.repository.BookingRepository
import com.study.booking.infrastructure.client.TripServiceClient
import com.study.booking.infrastructure.client.UserServiceClient
import com.study.common.event.*
import com.study.common.exception.EntityNotFoundException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookingService(
    private val bookingRepository: BookingRepository,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>,
    private val userServiceClient: UserServiceClient,
    private val tripServiceClient: TripServiceClient
) {
    @Transactional
    fun createBooking(userId: Long, request: CreateBookingRequest): BookingResponse {
        userServiceClient.verifyUserExists(userId)
        tripServiceClient.verifyTripExists(request.tripId!!)
        val booking = Booking(userId = userId, tripId = request.tripId)
        val saved = bookingRepository.save(booking)
        kafkaTemplate.send("booking-events", BookingCreatedEvent(saved.id, saved.userId, saved.tripId))
        return BookingResponse.from(saved)
    }

    fun getBooking(id: Long): BookingResponse =
        BookingResponse.from(bookingRepository.findById(id).orElseThrow { EntityNotFoundException("Booking", id) })

    fun getBookingsByUser(userId: Long): List<BookingResponse> =
        bookingRepository.findByUserId(userId).map { BookingResponse.from(it) }

    @Transactional
    fun confirmBooking(id: Long): BookingResponse {
        val booking = bookingRepository.findById(id).orElseThrow { EntityNotFoundException("Booking", id) }
        booking.confirm()
        kafkaTemplate.send("booking-events", BookingConfirmedEvent(booking.id, booking.userId, booking.tripId))
        return BookingResponse.from(booking)
    }

    @Transactional
    fun cancelBooking(id: Long): BookingResponse {
        val booking = bookingRepository.findById(id).orElseThrow { EntityNotFoundException("Booking", id) }
        booking.cancel()
        kafkaTemplate.send("booking-events", BookingCancelledEvent(booking.id, booking.userId, booking.tripId))
        return BookingResponse.from(booking)
    }
}
