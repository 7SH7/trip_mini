package com.study.feed.application.dto

import com.study.feed.domain.entity.Feed
import java.time.LocalDateTime

data class FeedResponse(
    val id: Long,
    val userId: Long,
    val content: String,
    val images: List<FeedImageResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(feed: Feed) = FeedResponse(
            id = feed.id,
            userId = feed.userId,
            content = feed.content,
            images = feed.images.map { FeedImageResponse(it.id, it.imageUrl, it.originalFileName) },
            createdAt = feed.createdAt,
            updatedAt = feed.updatedAt
        )
    }
}

data class FeedImageResponse(
    val id: Long,
    val imageUrl: String,
    val originalFileName: String?
)

data class CreateFeedRequest(
    val content: String
)

data class UpdateFeedRequest(
    val content: String
)
