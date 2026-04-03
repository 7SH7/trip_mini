package com.study.feed.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "feed_images")
class FeedImage(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    val feed: Feed,

    @Column(nullable = false)
    val imageUrl: String,

    val originalFileName: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
