package com.study.media.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "live_streams")
class LiveStream(
    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, unique = true)
    val streamKey: String = UUID.randomUUID().toString().replace("-", ""),

    @Column(nullable = false)
    var title: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LiveStreamStatus = LiveStreamStatus.IDLE,

    var rtmpIngestUrl: String? = null,
    var hlsPlaybackUrl: String? = null,
    var startedAt: LocalDateTime? = null,
    var endedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun goLive(rtmpUrl: String, hlsUrl: String) {
        status = LiveStreamStatus.LIVE
        rtmpIngestUrl = rtmpUrl
        hlsPlaybackUrl = hlsUrl
        startedAt = LocalDateTime.now()
    }

    fun end() {
        status = LiveStreamStatus.ENDED
        endedAt = LocalDateTime.now()
    }
}

enum class LiveStreamStatus {
    IDLE, LIVE, ENDED
}
