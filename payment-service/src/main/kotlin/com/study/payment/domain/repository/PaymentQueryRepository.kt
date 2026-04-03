package com.study.payment.domain.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.payment.domain.entity.Payment
import com.study.payment.domain.entity.PaymentStatus
import com.study.payment.domain.entity.QPayment
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class PaymentQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    private val payment = QPayment.payment

    fun searchPayments(
        userId: Long? = null,
        bookingId: Long? = null,
        status: PaymentStatus? = null,
        minAmount: BigDecimal? = null,
        maxAmount: BigDecimal? = null
    ): List<Payment> {
        val builder = BooleanBuilder()

        userId?.let { builder.and(payment.userId.eq(it)) }
        bookingId?.let { builder.and(payment.bookingId.eq(it)) }
        status?.let { builder.and(payment.status.eq(it)) }
        minAmount?.let { builder.and(payment.amount.goe(it)) }
        maxAmount?.let { builder.and(payment.amount.loe(it)) }

        return queryFactory
            .selectFrom(payment)
            .where(builder)
            .orderBy(payment.createdAt.desc())
            .fetch()
    }

    fun getTotalAmountByUser(userId: Long): BigDecimal {
        return queryFactory
            .select(payment.amount.sum())
            .from(payment)
            .where(payment.userId.eq(userId).and(payment.status.eq(PaymentStatus.COMPLETED)))
            .fetchOne() ?: BigDecimal.ZERO
    }
}
