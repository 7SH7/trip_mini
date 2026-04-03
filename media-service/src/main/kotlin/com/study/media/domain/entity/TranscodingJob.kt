package com.study.media.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "transcoding_jobs")
class TranscodingJob(
    @Column(nullable = false)
    val feedId: Long,

    @Column(nullable = false)
    val mediaId: Long,

    @Column(nullable = false)
    val inputS3Key: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TranscodingStatus = TranscodingStatus.QUEUED,

    var outputPrefix: String? = null,
    var errorMessage: String? = null,
    var startedAt: LocalDateTime? = null,
    var completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun start() {
        status = TranscodingStatus.PROCESSING
        startedAt = LocalDateTime.now()
    }

    fun complete(outputPrefix: String) {
        status = TranscodingStatus.COMPLETED
        this.outputPrefix = outputPrefix
        completedAt = LocalDateTime.now()
    }

    fun fail(error: String) {
        status = TranscodingStatus.FAILED
        errorMessage = error
        completedAt = LocalDateTime.now()
    }
}

enum class TranscodingStatus {
    QUEUED, PROCESSING, COMPLETED, FAILED
}
