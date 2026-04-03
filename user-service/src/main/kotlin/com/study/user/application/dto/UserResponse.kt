package com.study.user.application.dto

import com.study.user.domain.entity.User
import java.time.LocalDateTime

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            email = user.email,
            name = user.name,
            createdAt = user.createdAt
        )
    }
}
