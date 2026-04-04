package com.study.media.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.media.application.dto.CreateLiveStreamRequest
import com.study.media.application.dto.LiveStreamResponse
import com.study.media.application.service.LiveStreamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "라이브 스트리밍", description = "RTMP/HLS 라이브 방송 관리")
@RestController
@RequestMapping("/api/media/live")
class LiveStreamController(
    private val liveStreamService: LiveStreamService
) {
    @Operation(summary = "라이브 스트림 생성")
    @PostMapping("/streams")
    fun createStream(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateLiveStreamRequest
    ): ApiResponse<LiveStreamResponse> = ApiResponse.created(liveStreamService.createStream(userId, request))

    @Operation(summary = "활성 스트림 목록 조회")
    @GetMapping("/streams")
    fun getActiveStreams(): ApiResponse<List<LiveStreamResponse>> =
        ApiResponse.ok(liveStreamService.getActiveStreams())

    @Operation(summary = "스트림 상세 조회")
    @GetMapping("/streams/{id}")
    fun getStream(@PathVariable id: Long): ApiResponse<LiveStreamResponse> =
        ApiResponse.ok(liveStreamService.getStream(id))

    @Operation(summary = "내 스트림 목록 조회")
    @GetMapping("/streams/my")
    fun getMyStreams(@RequestHeader("X-User-Id") userId: Long): ApiResponse<List<LiveStreamResponse>> =
        ApiResponse.ok(liveStreamService.getMyStreams(userId))

    @Operation(summary = "RTMP 방송 시작 콜백 (Nginx-RTMP)")
    @PostMapping("/on-publish")
    fun onPublish(@RequestParam name: String): ResponseEntity<String> {
        val allowed = liveStreamService.onPublish(name)
        return if (allowed) ResponseEntity.ok("OK") else ResponseEntity.status(403).body("Forbidden")
    }

    @Operation(summary = "RTMP 방송 종료 콜백 (Nginx-RTMP)")
    @PostMapping("/on-done")
    fun onDone(@RequestParam name: String): ResponseEntity<String> {
        liveStreamService.onDone(name)
        return ResponseEntity.ok("OK")
    }
}
