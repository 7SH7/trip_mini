package com.study.booking.infrastructure.mybatis

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface BookingMapper {
    fun getBookingCountByStatus(@Param("userId") userId: Long): List<Map<String, Any>>
    fun getMostBookedTrips(@Param("limit") limit: Int): List<Map<String, Any>>
}
