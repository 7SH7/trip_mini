package com.study.booking.infrastructure.scheduler

import com.study.booking.domain.entity.BookingStatus
import com.study.booking.domain.repository.BookingRepository
import com.study.common.event.BookingCancelledEvent
import com.study.common.outbox.OutboxEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class BookingTimeoutScheduler(
    private val bookingRepository: BookingRepository,
    private val outboxEventPublisher: OutboxEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 60000) // every 1 minute
    @Transactional
    fun cancelTimedOutBookings() {
        val timeout = LocalDateTime.now().minusMinutes(30)
        val pendingBookings = bookingRepository.findByStatusAndCreatedAtBefore(
            BookingStatus.PENDING, timeout
        )

        pendingBookings.forEach { booking ->
            booking.cancel()
            bookingRepository.save(booking)
            outboxEventPublisher.publish("booking-events",
                BookingCancelledEvent(booking.id, booking.userId, booking.tripId))
            log.info("Booking {} auto-cancelled due to 30min timeout", booking.id)
        }

        if (pendingBookings.isNotEmpty()) {
            log.info("Auto-cancelled {} timed-out bookings", pendingBookings.size)
        }
    }
}
