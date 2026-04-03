package com.study.payment.infrastructure.mybatis

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface PaymentMapper {
    fun getDailyRevenue(@Param("startDate") startDate: String): List<Map<String, Any>>
    fun getPaymentCountByStatus(@Param("userId") userId: Long): List<Map<String, Any>>
}
