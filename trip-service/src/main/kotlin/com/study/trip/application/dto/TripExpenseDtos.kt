package com.study.trip.application.dto

import com.study.trip.domain.entity.TripExpense
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class TripExpenseResponse(
    val id: Long,
    val tripId: Long,
    val userId: Long,
    val category: String,
    val amount: BigDecimal,
    val description: String?,
    val date: LocalDate,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(e: TripExpense) = TripExpenseResponse(
            e.id, e.tripId, e.userId, e.category, e.amount, e.description, e.date, e.createdAt
        )
    }
}

data class CreateExpenseRequest(
    @field:NotBlank val category: String = "",
    @field:NotNull val amount: BigDecimal? = null,
    val description: String? = null,
    @field:NotNull val date: LocalDate? = null
)

data class UpdateExpenseRequest(
    @field:NotBlank val category: String = "",
    @field:NotNull val amount: BigDecimal? = null,
    val description: String? = null,
    @field:NotNull val date: LocalDate? = null
)

data class ExpenseSummaryResponse(
    val totalExpense: BigDecimal,
    val byCategory: Map<String, BigDecimal>,
    val byUser: Map<Long, BigDecimal>
)

data class ExpenseSplitResponse(
    val userId: Long,
    val amount: BigDecimal
)

data class UpdateSplitRequest(
    val splits: List<SplitEntry> = emptyList()
) {
    data class SplitEntry(val userId: Long, val amount: BigDecimal)
}

data class SettlementResponse(
    val settlements: List<SettlementEntry>
) {
    data class SettlementEntry(
        val fromUserId: Long,
        val toUserId: Long,
        val amount: BigDecimal
    )
}
