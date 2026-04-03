package com.study.common.dto

data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(200, "OK", data)
        fun <T> created(data: T): ApiResponse<T> = ApiResponse(201, "Created", data)
        fun <T> error(status: Int, message: String): ApiResponse<T> = ApiResponse(status, message, null)
    }
}
