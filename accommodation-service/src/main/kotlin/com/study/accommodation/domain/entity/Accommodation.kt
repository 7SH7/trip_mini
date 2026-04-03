package com.study.accommodation.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "accommodations")
class Accommodation(
    @Column(nullable = false, unique = true)
    val contentId: String,

    @Column(nullable = false)
    val title: String,

    val address: String? = null,
    val areaCode: String? = null,
    val sigunguCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String? = null,
    val tel: String? = null,
    val price: Int? = null,
    val priceRaw: String? = null,
    val category: String? = null,
    val overview: String? = null,

    var lastSyncedAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
