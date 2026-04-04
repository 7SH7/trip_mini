package com.study.media.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.study.media.application.service.TranscodingService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class VideoUploadConsumer(
    private val transcodingService: TranscodingService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["media-events"], groupId = "media-service")
    @Transactional
    fun handleMediaEvent(message: String) {
        try {
            val event = objectMapper.readTree(message)
            val eventType = event.get("eventType")?.asText() ?: return

            if (eventType == "VideoUploadedEvent") {
                val feedId = event.get("feedId")?.asLong() ?: return
                val mediaId = event.get("mediaId")?.asLong() ?: return
                val s3Key = event.get("s3Key")?.asText() ?: return

                log.info("Received VideoUploadedEvent: feedId={}, mediaId={}, s3Key={}", feedId, mediaId, s3Key)

                val job = transcodingService.createJob(feedId, mediaId, s3Key)
                log.info("Transcoding job {} created for media {}", job.id, mediaId)

                transcodingService.processJob(job.id)
            }
        } catch (e: Exception) {
            log.error("Failed to process media event", e)
        }
    }
}
