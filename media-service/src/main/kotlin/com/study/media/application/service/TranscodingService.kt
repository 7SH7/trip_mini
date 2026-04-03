package com.study.media.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.media.application.dto.TranscodingJobResponse
import com.study.media.domain.entity.TranscodingJob
import com.study.media.domain.repository.TranscodingJobRepository
import com.study.media.infrastructure.transcoding.FFmpegTranscoder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TranscodingService(
    private val transcodingJobRepository: TranscodingJobRepository,
    private val ffmpegTranscoder: FFmpegTranscoder
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getJob(id: Long): TranscodingJobResponse {
        val job = transcodingJobRepository.findById(id)
            .orElseThrow { EntityNotFoundException("TranscodingJob", id) }
        return TranscodingJobResponse.from(job)
    }

    @Transactional
    fun createJob(feedId: Long, mediaId: Long, inputS3Key: String): TranscodingJob {
        return transcodingJobRepository.save(
            TranscodingJob(feedId = feedId, mediaId = mediaId, inputS3Key = inputS3Key)
        )
    }
}
