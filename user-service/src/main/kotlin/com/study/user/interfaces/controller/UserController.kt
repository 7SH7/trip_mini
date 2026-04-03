package com.study.user.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.user.application.dto.CreateUserRequest
import com.study.user.application.dto.UserResponse
import com.study.user.application.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ApiResponse<UserResponse> =
        ApiResponse.created(userService.createUser(request))

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ApiResponse<UserResponse> =
        ApiResponse.ok(userService.getUser(id))

    @GetMapping("/me")
    fun getMe(@RequestHeader("X-User-Id") userId: Long): ApiResponse<UserResponse> =
        ApiResponse.ok(userService.getUser(userId))
}
