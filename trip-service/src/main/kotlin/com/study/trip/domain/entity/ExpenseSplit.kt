package com.study.trip.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "expense_splits")
class ExpenseSplit(
    @Column(nullable = false)
    val expenseId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    var amount: BigDecimal,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
