package com.study.trip.domain.repository

import com.study.trip.domain.entity.TripExpense
import org.springframework.data.jpa.repository.JpaRepository

interface TripExpenseRepository : JpaRepository<TripExpense, Long> {
    fun findByTripIdOrderByDateDesc(tripId: Long): List<TripExpense>
}
