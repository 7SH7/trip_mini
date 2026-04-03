package com.study.media.application.dto

import com.study.media.domain.entity.LiveStream
import com.study.media.domain.entity.LiveStreamStatus
import com.study.media.domain.entity.TranscodingJob
import com.study.media.domain.entity.TranscodingStatus
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class TranscodingJobResponse(
    val id: Long,
    val feedId: Long,
    val mediaId: Long,
    val status: TranscodingStatus,
    val outputPrefix: String?,
    val errorMessage: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(job: TranscodingJob) = TranscodingJobResponse(
            id = job.id, feedId = job.feedId, mediaId = job.mediaId,
            status = job.status, outputPrefix = job.outputPrefix,
            errorMessage = job.errorMessage, createdAt = job.createdAt
        )
    }
}

data class CreateLiveStreamRequest(
    @field:NotBlank val title: String = ""
)

data class LiveStreamResponse(
    val id: Long,
    val userId: Long,
    val streamKey: String,
    val title: String,
    val status: LiveStreamStatus,
    val rtmpIngestUrl: String?,
    val hlsPlaybackUrl: String?,
    val startedAt: LocalDateTime?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(stream: LiveStream) = LiveStreamResponse(
            id = stream.id, userId = stream.userId, streamKey = stream.streamKey,
            title = stream.title, status = stream.status,
            rtmpIngestUrl = stream.rtmpIngestUrl, hlsPlaybackUrl = stream.hlsPlaybackUrl,
            startedAt = stream.startedAt, createdAt = stream.createdAt
        )
    }
}
