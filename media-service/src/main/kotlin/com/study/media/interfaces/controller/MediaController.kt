package com.study.media.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.media.application.dto.TranscodingJobResponse
import com.study.media.application.service.TranscodingService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/media")
class MediaController(
    private val transcodingService: TranscodingService
) {
    @GetMapping("/transcoding/{id}")
    fun getTranscodingJob(@PathVariable id: Long): ApiResponse<TranscodingJobResponse> =
        ApiResponse.ok(transcodingService.getJob(id))
}
