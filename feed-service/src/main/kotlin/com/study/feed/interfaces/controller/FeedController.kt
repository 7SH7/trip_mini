package com.study.feed.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.feed.application.dto.CreateFeedRequest
import com.study.feed.application.dto.FeedResponse
import com.study.feed.application.dto.UpdateFeedRequest
import com.study.feed.application.service.FeedService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "피드", description = "여행 피드 CRUD, 이미지 업로드")
@RestController
@RequestMapping("/api/feeds")
class FeedController(
    private val feedService: FeedService
) {
    @Operation(summary = "피드 생성 (이미지 업로드 포함)")
    @PostMapping
    fun createFeed(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestPart("feed") request: CreateFeedRequest,
        @RequestPart("images", required = false) images: List<MultipartFile>?
    ): ApiResponse<FeedResponse> = ApiResponse.created(feedService.createFeed(userId, request, images))

    @Operation(summary = "전체 피드 목록 조회")
    @GetMapping
    fun getFeeds(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<Page<FeedResponse>> = ApiResponse.ok(feedService.getFeeds(page, size))

    @Operation(summary = "내 피드 목록 조회")
    @GetMapping("/my")
    fun getMyFeeds(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<Page<FeedResponse>> = ApiResponse.ok(feedService.getMyFeeds(userId, page, size))

    @Operation(summary = "피드 단건 조회")
    @GetMapping("/{id}")
    fun getFeed(@PathVariable id: Long): ApiResponse<FeedResponse> =
        ApiResponse.ok(feedService.getFeed(id))

    @Operation(summary = "피드 수정")
    @PutMapping("/{id}")
    fun updateFeed(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long,
        @RequestBody request: UpdateFeedRequest
    ): ApiResponse<FeedResponse> = ApiResponse.ok(feedService.updateFeed(userId, id, request))

    @Operation(summary = "피드 삭제")
    @DeleteMapping("/{id}")
    fun deleteFeed(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long
    ): ApiResponse<Unit?> {
        feedService.deleteFeed(userId, id)
        return ApiResponse(200, "Deleted", null)
    }
}
