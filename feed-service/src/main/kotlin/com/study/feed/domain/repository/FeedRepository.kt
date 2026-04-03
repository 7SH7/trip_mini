package com.study.feed.domain.repository

import com.study.feed.domain.entity.Feed
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FeedRepository : JpaRepository<Feed, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<Feed>
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<Feed>
}
