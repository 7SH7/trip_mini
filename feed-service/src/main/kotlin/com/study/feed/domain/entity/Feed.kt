package com.study.feed.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "feeds")
class Feed(
    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToMany(mappedBy = "feed", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableList<FeedImage> = mutableListOf()
) {
    fun update(content: String) {
        this.content = content
        this.updatedAt = LocalDateTime.now()
    }

    fun addImage(imageUrl: String, originalFileName: String) {
        images.add(FeedImage(feed = this, imageUrl = imageUrl, originalFileName = originalFileName))
    }
}
