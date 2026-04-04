package com.study.trip.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.common.exception.InvalidRequestException
import com.study.trip.application.dto.*
import com.study.trip.domain.entity.TripExpense
import com.study.trip.domain.repository.TripExpenseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class TripExpenseService(
    private val expenseRepository: TripExpenseRepository,
    private val memberService: TripMemberService
) {
    fun getExpenses(tripId: Long): List<TripExpenseResponse> =
        expenseRepository.findByTripIdOrderByDateDesc(tripId).map { TripExpenseResponse.from(it) }

    fun getSummary(tripId: Long): ExpenseSummaryResponse {
        val expenses = expenseRepository.findByTripIdOrderByDateDesc(tripId)
        val total = expenses.fold(BigDecimal.ZERO) { acc, e -> acc + e.amount }
        val byCategory = expenses.groupBy { it.category }.mapValues { (_, list) -> list.fold(BigDecimal.ZERO) { acc, e -> acc + e.amount } }
        val byUser = expenses.groupBy { it.userId }.mapValues { (_, list) -> list.fold(BigDecimal.ZERO) { acc, e -> acc + e.amount } }
        return ExpenseSummaryResponse(total, byCategory, byUser)
    }

    @Transactional
    fun create(tripId: Long, userId: Long, request: CreateExpenseRequest): TripExpenseResponse {
        if (!memberService.isMember(tripId, userId)) throw InvalidRequestException("여행 멤버가 아닙니다.")
        val expense = expenseRepository.save(TripExpense(
            tripId = tripId, userId = userId, category = request.category,
            amount = requireNotNull(request.amount), description = request.description,
            date = requireNotNull(request.date)
        ))
        return TripExpenseResponse.from(expense)
    }

    @Transactional
    fun update(tripId: Long, expenseId: Long, userId: Long, request: UpdateExpenseRequest): TripExpenseResponse {
        if (!memberService.isMember(tripId, userId)) throw InvalidRequestException("여행 멤버가 아닙니다.")
        val expense = expenseRepository.findById(expenseId).orElseThrow { EntityNotFoundException("TripExpense", expenseId) }
        expense.update(request.category, requireNotNull(request.amount), request.description, requireNotNull(request.date))
        return TripExpenseResponse.from(expense)
    }

    @Transactional
    fun delete(tripId: Long, expenseId: Long, userId: Long) {
        if (!memberService.isMember(tripId, userId)) throw InvalidRequestException("여행 멤버가 아닙니다.")
        expenseRepository.deleteById(expenseId)
    }
}
