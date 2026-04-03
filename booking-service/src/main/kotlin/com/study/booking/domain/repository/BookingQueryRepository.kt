package com.study.booking.domain.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.booking.domain.entity.Booking
import com.study.booking.domain.entity.BookingStatus
import com.study.booking.domain.entity.QBooking
import org.springframework.stereotype.Repository

@Repository
class BookingQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    private val booking = QBooking.booking

    fun searchBookings(
        userId: Long? = null,
        tripId: Long? = null,
        status: BookingStatus? = null
    ): List<Booking> {
        val builder = BooleanBuilder()

        userId?.let { builder.and(booking.userId.eq(it)) }
        tripId?.let { builder.and(booking.tripId.eq(it)) }
        status?.let { builder.and(booking.status.eq(it)) }

        return queryFactory
            .selectFrom(booking)
            .where(builder)
            .orderBy(booking.createdAt.desc())
            .fetch()
    }
}
