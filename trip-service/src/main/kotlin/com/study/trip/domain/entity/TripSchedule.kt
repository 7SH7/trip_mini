package com.study.trip.domain.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "trip_schedules")
class TripSchedule(
    @Column(nullable = false)
    val tripId: Long,

    @Column(nullable = false)
    var date: LocalDate,

    @Column(nullable = false)
    var title: String,

    var memo: String? = null,
    var startTime: LocalTime? = null,
    var endTime: LocalTime? = null,
    var orderIndex: Int = 0,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun update(title: String, memo: String?, date: LocalDate, startTime: LocalTime?, endTime: LocalTime?, orderIndex: Int) {
        this.title = title
        this.memo = memo
        this.date = date
        this.startTime = startTime
        this.endTime = endTime
        this.orderIndex = orderIndex
    }
}
