package com.study.accommodation.interfaces.controller

import com.study.accommodation.application.dto.AccommodationResponse
import com.study.accommodation.application.service.AccommodationService
import com.study.common.dto.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/accommodations")
class AccommodationController(
    private val accommodationService: AccommodationService
) {
    @GetMapping("/search")
    fun search(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) areaCode: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<Page<AccommodationResponse>> =
        ApiResponse.ok(accommodationService.search(keyword, areaCode, page, size))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ApiResponse<AccommodationResponse> =
        ApiResponse.ok(accommodationService.getById(id))
}
