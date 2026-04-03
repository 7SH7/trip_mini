package com.study.feed.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.common.exception.InvalidRequestException
import com.study.feed.application.dto.CreateFeedRequest
import com.study.feed.application.dto.FeedResponse
import com.study.feed.application.dto.UpdateFeedRequest
import com.study.feed.domain.entity.Feed
import com.study.feed.domain.repository.FeedRepository
import com.study.feed.infrastructure.storage.S3StorageClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class FeedService(
    private val feedRepository: FeedRepository,
    private val s3StorageClient: S3StorageClient
) {
    @Transactional
    fun createFeed(userId: Long, request: CreateFeedRequest, images: List<MultipartFile>?): FeedResponse {
        val feed = Feed(userId = userId, content = request.content)

        images?.forEach { file ->
            val imageUrl = s3StorageClient.upload(file)
            feed.addImage(imageUrl, file.originalFilename ?: "unknown")
        }

        val saved = feedRepository.save(feed)
        return FeedResponse.from(saved)
    }

    fun getFeed(id: Long): FeedResponse {
        val feed = feedRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Feed", id) }
        return FeedResponse.from(feed)
    }

    fun getFeeds(page: Int, size: Int): Page<FeedResponse> =
        feedRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
            .map { FeedResponse.from(it) }

    fun getMyFeeds(userId: Long, page: Int, size: Int): Page<FeedResponse> =
        feedRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
            .map { FeedResponse.from(it) }

    @Transactional
    fun updateFeed(userId: Long, id: Long, request: UpdateFeedRequest): FeedResponse {
        val feed = feedRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Feed", id) }
        if (feed.userId != userId) throw InvalidRequestException("You can only edit your own feed")
        feed.update(request.content)
        return FeedResponse.from(feed)
    }

    @Transactional
    fun deleteFeed(userId: Long, id: Long) {
        val feed = feedRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Feed", id) }
        if (feed.userId != userId) throw InvalidRequestException("You can only delete your own feed")
        feedRepository.delete(feed)
    }
}
