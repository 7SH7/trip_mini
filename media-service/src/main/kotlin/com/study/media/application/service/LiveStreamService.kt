package com.study.media.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.media.application.dto.CreateLiveStreamRequest
import com.study.media.application.dto.LiveStreamResponse
import com.study.media.domain.entity.LiveStream
import com.study.media.domain.entity.LiveStreamStatus
import com.study.media.domain.repository.LiveStreamRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LiveStreamService(
    private val liveStreamRepository: LiveStreamRepository,
    @Value("\${media.rtmp-base-url}") private val rtmpBaseUrl: String,
    @Value("\${media.hls-base-url}") private val hlsBaseUrl: String
) {
    @Transactional
    fun createStream(userId: Long, request: CreateLiveStreamRequest): LiveStreamResponse {
        val stream = liveStreamRepository.save(
            LiveStream(userId = userId, title = request.title)
        )
        return LiveStreamResponse.from(stream).copy(
            rtmpIngestUrl = "$rtmpBaseUrl/${stream.streamKey}"
        )
    }

    fun getActiveStreams(): List<LiveStreamResponse> =
        liveStreamRepository.findByStatus(LiveStreamStatus.LIVE).map { LiveStreamResponse.from(it) }

    fun getStream(id: Long): LiveStreamResponse {
        val stream = liveStreamRepository.findById(id)
            .orElseThrow { EntityNotFoundException("LiveStream", id) }
        return LiveStreamResponse.from(stream)
    }

    fun getMyStreams(userId: Long): List<LiveStreamResponse> =
        liveStreamRepository.findByUserId(userId).map { LiveStreamResponse.from(it) }

    @Transactional
    fun onPublish(streamKey: String): Boolean {
        val stream = liveStreamRepository.findByStreamKey(streamKey).orElse(null) ?: return false
        stream.goLive("$rtmpBaseUrl/$streamKey", "$hlsBaseUrl/$streamKey/index.m3u8")
        return true
    }

    @Transactional
    fun onDone(streamKey: String) {
        liveStreamRepository.findByStreamKey(streamKey).ifPresent { it.end() }
    }
}
