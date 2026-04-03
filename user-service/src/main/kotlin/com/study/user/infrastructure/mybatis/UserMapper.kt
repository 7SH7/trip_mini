package com.study.user.infrastructure.mybatis

import com.study.user.domain.entity.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface UserMapper {
    fun countUsersByProvider(): List<Map<String, Any>>
    fun findRecentUsers(@Param("limit") limit: Int): List<User>
}
