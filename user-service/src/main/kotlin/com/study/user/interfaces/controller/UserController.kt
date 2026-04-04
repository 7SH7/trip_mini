package com.study.user.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.user.application.dto.CreateUserRequest
import com.study.user.application.dto.UserResponse
import com.study.user.application.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "사용자", description = "사용자 생성 및 조회")
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @Operation(summary = "사용자 생성")
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ApiResponse<UserResponse> =
        ApiResponse.created(userService.createUser(request))

    @Operation(summary = "사용자 조회")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ApiResponse<UserResponse> =
        ApiResponse.ok(userService.getUser(id))

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun getMe(@RequestHeader("X-User-Id") userId: Long): ApiResponse<UserResponse> =
        ApiResponse.ok(userService.getUser(userId))
}
