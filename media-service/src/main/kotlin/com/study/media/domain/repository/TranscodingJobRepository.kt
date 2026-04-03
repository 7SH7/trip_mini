package com.study.media.domain.repository

import com.study.media.domain.entity.TranscodingJob
import com.study.media.domain.entity.TranscodingStatus
import org.springframework.data.jpa.repository.JpaRepository

interface TranscodingJobRepository : JpaRepository<TranscodingJob, Long> {
    fun findByStatus(status: TranscodingStatus): List<TranscodingJob>
}
