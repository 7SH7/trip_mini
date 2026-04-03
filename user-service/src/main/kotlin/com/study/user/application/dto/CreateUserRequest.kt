package com.study.user.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String = "",

    @field:NotBlank(message = "Name is required")
    val name: String = "",

    @field:NotBlank(message = "Password is required")
    val password: String = ""
)
