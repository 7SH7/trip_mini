package com.study.media.domain.repository

import com.study.media.domain.entity.LiveStream
import com.study.media.domain.entity.LiveStreamStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface LiveStreamRepository : JpaRepository<LiveStream, Long> {
    fun findByStreamKey(streamKey: String): Optional<LiveStream>
    fun findByStatus(status: LiveStreamStatus): List<LiveStream>
    fun findByUserId(userId: Long): List<LiveStream>
}
