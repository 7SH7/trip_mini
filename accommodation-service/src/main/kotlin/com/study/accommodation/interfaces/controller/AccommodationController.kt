package com.study.accommodation.interfaces.controller

import com.study.accommodation.application.dto.AccommodationResponse
import com.study.accommodation.application.service.AccommodationService
import com.study.common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*

@Tag(name = "숙소", description = "한국관광공사 API 기반 숙소 검색")
@RestController
@RequestMapping("/api/accommodations")
class AccommodationController(
    private val accommodationService: AccommodationService
) {
    @Operation(summary = "숙소 검색")
    @GetMapping("/search")
    fun search(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) areaCode: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<Page<AccommodationResponse>> =
        ApiResponse.ok(accommodationService.search(keyword, areaCode, page, size))

    @Operation(summary = "숙소 상세 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ApiResponse<AccommodationResponse> =
        ApiResponse.ok(accommodationService.getById(id))
}
