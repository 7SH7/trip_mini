package com.study.trip.domain.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.trip.domain.entity.QTrip
import com.study.trip.domain.entity.Trip
import com.study.trip.domain.entity.TripStatus
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class TripQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    private val trip = QTrip.trip

    fun searchTrips(
        userId: Long? = null,
        status: TripStatus? = null,
        keyword: String? = null,
        startDateFrom: LocalDate? = null,
        startDateTo: LocalDate? = null
    ): List<Trip> {
        val builder = BooleanBuilder()

        userId?.let { builder.and(trip.userId.eq(it)) }
        status?.let { builder.and(trip.status.eq(it)) }
        keyword?.let { builder.and(trip.title.containsIgnoreCase(it).or(trip.description.containsIgnoreCase(it))) }
        startDateFrom?.let { builder.and(trip.startDate.goe(it)) }
        startDateTo?.let { builder.and(trip.startDate.loe(it)) }

        return queryFactory
            .selectFrom(trip)
            .where(builder)
            .orderBy(trip.createdAt.desc())
            .fetch()
    }
}
