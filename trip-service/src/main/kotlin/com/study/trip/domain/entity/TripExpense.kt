package com.study.trip.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "trip_expenses")
class TripExpense(
    @Column(nullable = false)
    val tripId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    var category: String,

    @Column(nullable = false)
    var amount: BigDecimal,

    var description: String? = null,

    @Column(nullable = false)
    var date: LocalDate,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun update(category: String, amount: BigDecimal, description: String?, date: LocalDate) {
        this.category = category
        this.amount = amount
        this.description = description
        this.date = date
    }
}
