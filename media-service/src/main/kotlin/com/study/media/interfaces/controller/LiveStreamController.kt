package com.study.media.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.media.application.dto.CreateLiveStreamRequest
import com.study.media.application.dto.LiveStreamResponse
import com.study.media.application.service.LiveStreamService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/media/live")
class LiveStreamController(
    private val liveStreamService: LiveStreamService
) {
    @PostMapping("/streams")
    fun createStream(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateLiveStreamRequest
    ): ApiResponse<LiveStreamResponse> = ApiResponse.created(liveStreamService.createStream(userId, request))

    @GetMapping("/streams")
    fun getActiveStreams(): ApiResponse<List<LiveStreamResponse>> =
        ApiResponse.ok(liveStreamService.getActiveStreams())

    @GetMapping("/streams/{id}")
    fun getStream(@PathVariable id: Long): ApiResponse<LiveStreamResponse> =
        ApiResponse.ok(liveStreamService.getStream(id))

    @GetMapping("/streams/my")
    fun getMyStreams(@RequestHeader("X-User-Id") userId: Long): ApiResponse<List<LiveStreamResponse>> =
        ApiResponse.ok(liveStreamService.getMyStreams(userId))

    // Nginx-RTMP callbacks
    @PostMapping("/on-publish")
    fun onPublish(@RequestParam name: String): ResponseEntity<String> {
        val allowed = liveStreamService.onPublish(name)
        return if (allowed) ResponseEntity.ok("OK") else ResponseEntity.status(403).body("Forbidden")
    }

    @PostMapping("/on-done")
    fun onDone(@RequestParam name: String): ResponseEntity<String> {
        liveStreamService.onDone(name)
        return ResponseEntity.ok("OK")
    }
}
