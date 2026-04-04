package com.study.trip.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.common.exception.InvalidRequestException
import com.study.trip.application.dto.*
import com.study.trip.domain.entity.ExpenseSplit
import com.study.trip.domain.entity.MemberRole
import com.study.trip.domain.entity.TripExpense
import com.study.trip.domain.repository.ExpenseSplitRepository
import com.study.trip.domain.repository.TripExpenseRepository
import com.study.trip.domain.repository.TripMemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
@Transactional(readOnly = true)
class TripExpenseService(
    private val expenseRepository: TripExpenseRepository,
    private val splitRepository: ExpenseSplitRepository,
    private val memberRepository: TripMemberRepository,
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
        // 기본 N빵 split 생성
        val members = memberRepository.findByTripId(tripId)
        val splitAmount = expense.amount.divide(BigDecimal(members.size), 0, RoundingMode.CEILING)
        members.forEach { m ->
            splitRepository.save(ExpenseSplit(expenseId = expense.id, userId = m.userId, amount = splitAmount))
        }
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
        splitRepository.deleteByExpenseId(expenseId)
        expenseRepository.deleteById(expenseId)
    }

    fun getSplits(expenseId: Long): List<ExpenseSplitResponse> =
        splitRepository.findByExpenseId(expenseId).map { ExpenseSplitResponse(it.userId, it.amount) }

    // OWNER만 멤버별 부담금 수정 가능
    @Transactional
    fun updateSplits(tripId: Long, expenseId: Long, userId: Long, request: UpdateSplitRequest): List<ExpenseSplitResponse> {
        val member = memberRepository.findByTripIdAndUserId(tripId, userId)
            ?: throw InvalidRequestException("여행 멤버가 아닙니다.")
        if (member.role != MemberRole.OWNER)
            throw InvalidRequestException("리더만 정산 금액을 수정할 수 있습니다.")

        splitRepository.deleteByExpenseId(expenseId)
        val splits = request.splits.map { s ->
            splitRepository.save(ExpenseSplit(expenseId = expenseId, userId = s.userId, amount = s.amount))
        }
        return splits.map { ExpenseSplitResponse(it.userId, it.amount) }
    }

    // 최종 정산: 누가 누구에게 얼마 보내야 하는지
    fun getSettlement(tripId: Long): SettlementResponse {
        val expenses = expenseRepository.findByTripIdOrderByDateDesc(tripId)
        val expenseIds = expenses.map { it.id }
        val allSplits = if (expenseIds.isNotEmpty()) splitRepository.findByExpenseIdIn(expenseIds) else emptyList()

        // 각 멤버가 실제로 낸 금액
        val paid = mutableMapOf<Long, BigDecimal>()
        expenses.forEach { e -> paid[e.userId] = (paid[e.userId] ?: BigDecimal.ZERO) + e.amount }

        // 각 멤버가 내야 할 금액 (split 합산)
        val owed = mutableMapOf<Long, BigDecimal>()
        allSplits.forEach { s -> owed[s.userId] = (owed[s.userId] ?: BigDecimal.ZERO) + s.amount }

        // 차액: 양수 = 받을 돈, 음수 = 보낼 돈
        val allUserIds = (paid.keys + owed.keys).toSet()
        val balance = allUserIds.associateWith { uid ->
            (paid[uid] ?: BigDecimal.ZERO) - (owed[uid] ?: BigDecimal.ZERO)
        }

        // Greedy 정산 계산
        val creditors = balance.filter { it.value > BigDecimal.ZERO }.toMutableMap()
        val debtors = balance.filter { it.value < BigDecimal.ZERO }.mapValues { it.value.negate() }.toMutableMap()
        val settlements = mutableListOf<SettlementResponse.SettlementEntry>()

        for ((creditor, _) in creditors.toMap()) {
            for ((debtor, _) in debtors.toMap()) {
                val creditLeft = creditors[creditor] ?: continue
                val debtLeft = debtors[debtor] ?: continue
                if (creditLeft <= BigDecimal.ZERO || debtLeft <= BigDecimal.ZERO) continue

                val transfer = creditLeft.min(debtLeft)
                settlements.add(SettlementResponse.SettlementEntry(debtor, creditor, transfer))
                creditors[creditor] = creditLeft - transfer
                debtors[debtor] = debtLeft - transfer
            }
        }

        return SettlementResponse(settlements)
    }
}
