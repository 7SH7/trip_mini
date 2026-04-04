package com.study.trip.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "trip_places")
class TripPlace(
    @Column(nullable = false)
    val tripId: Long,

    @Column(nullable = false)
    var name: String,

    var address: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var category: String? = null,
    var notes: String? = null,
    val addedBy: Long? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun update(name: String, address: String?, latitude: Double?, longitude: Double?, category: String?, notes: String?) {
        this.name = name
        this.address = address
        this.latitude = latitude
        this.longitude = longitude
        this.category = category
        this.notes = notes
    }
}
