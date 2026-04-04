package com.study.media.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.media.application.dto.TranscodingJobResponse
import com.study.media.application.service.TranscodingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "미디어", description = "트랜스코딩 작업 조회")
@RestController
@RequestMapping("/api/media")
class MediaController(
    private val transcodingService: TranscodingService
) {
    @Operation(summary = "트랜스코딩 작업 상태 조회")
    @GetMapping("/transcoding/{id}")
    fun getTranscodingJob(@PathVariable id: Long): ApiResponse<TranscodingJobResponse> =
        ApiResponse.ok(transcodingService.getJob(id))
}
