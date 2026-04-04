package com.study.trip.domain.repository

import com.study.trip.domain.entity.ExpenseSplit
import org.springframework.data.jpa.repository.JpaRepository

interface ExpenseSplitRepository : JpaRepository<ExpenseSplit, Long> {
    fun findByExpenseId(expenseId: Long): List<ExpenseSplit>
    fun findByExpenseIdIn(expenseIds: List<Long>): List<ExpenseSplit>
    fun deleteByExpenseId(expenseId: Long)
}
