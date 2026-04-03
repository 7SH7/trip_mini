package com.study.feed.interfaces.controller

import com.study.common.dto.ApiResponse
import com.study.feed.application.dto.CreateFeedRequest
import com.study.feed.application.dto.FeedResponse
import com.study.feed.application.dto.UpdateFeedRequest
import com.study.feed.application.service.FeedService
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/feeds")
class FeedController(
    private val feedService: FeedService
) {
    @PostMapping
    fun createFeed(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestPart("feed") request: CreateFeedRequest,
        @RequestPart("images", required = false) images: List<MultipartFile>?
    ): ApiResponse<FeedResponse> = ApiResponse.created(feedService.createFeed(userId, request, images))

    @GetMapping
    fun getFeeds(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<Page<FeedResponse>> = ApiResponse.ok(feedService.getFeeds(page, size))

    @GetMapping("/my")
    fun getMyFeeds(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<Page<FeedResponse>> = ApiResponse.ok(feedService.getMyFeeds(userId, page, size))

    @GetMapping("/{id}")
    fun getFeed(@PathVariable id: Long): ApiResponse<FeedResponse> =
        ApiResponse.ok(feedService.getFeed(id))

    @PutMapping("/{id}")
    fun updateFeed(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long,
        @RequestBody request: UpdateFeedRequest
    ): ApiResponse<FeedResponse> = ApiResponse.ok(feedService.updateFeed(userId, id, request))

    @DeleteMapping("/{id}")
    fun deleteFeed(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable id: Long
    ): ApiResponse<Unit?> {
        feedService.deleteFeed(userId, id)
        return ApiResponse(200, "Deleted", null)
    }
}
