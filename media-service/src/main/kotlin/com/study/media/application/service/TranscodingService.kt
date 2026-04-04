package com.study.media.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.media.application.dto.TranscodingJobResponse
import com.study.media.domain.entity.TranscodingJob
import com.study.media.domain.repository.TranscodingJobRepository
import com.study.media.infrastructure.storage.S3StorageClient
import com.study.media.infrastructure.transcoding.FFmpegTranscoder
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Service
@Transactional(readOnly = true)
class TranscodingService(
    private val transcodingJobRepository: TranscodingJobRepository,
    private val ffmpegTranscoder: FFmpegTranscoder,
    private val s3StorageClient: S3StorageClient
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

    @Async
    fun processJob(jobId: Long) {
        val job = transcodingJobRepository.findById(jobId).orElse(null) ?: return

        val tempDir = Files.createTempDirectory("transcode-$jobId")
        val inputFile = tempDir.resolve("input.mp4").toFile()

        try {
            s3StorageClient.download(job.inputS3Key, inputFile)

            job.start()
            transcodingJobRepository.save(job)

            val result = ffmpegTranscoder.transcode(inputFile, jobId)

            if (result.success) {
                val outputPrefix = "transcoded/${job.feedId}/${job.mediaId}"
                val outputDir = File(result.outputDir)

                outputDir.listFiles()?.forEach { file ->
                    val contentType = when {
                        file.name.endsWith(".m3u8") -> "application/vnd.apple.mpegurl"
                        file.name.endsWith(".ts") -> "video/mp2t"
                        else -> "application/octet-stream"
                    }
                    s3StorageClient.upload(file, "$outputPrefix/${file.name}", contentType)
                }

                job.complete(outputPrefix)
                log.info("Transcoding job {} completed successfully", jobId)
            } else {
                job.fail(result.error ?: "Unknown transcoding error")
                log.error("Transcoding job {} failed: {}", jobId, result.error)
            }
            transcodingJobRepository.save(job)
        } catch (e: Exception) {
            log.error("Transcoding pipeline failed for job {}", jobId, e)
            job.fail(e.message ?: "Pipeline error")
            transcodingJobRepository.save(job)
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
}
