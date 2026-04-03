package com.study.chat.domain.repository

import com.study.chat.domain.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {

    @Query("""
        SELECT r FROM ChatRoom r
        WHERE (6371 * acos(
            cos(radians(:lat)) * cos(radians(r.centerLatitude))
            * cos(radians(r.centerLongitude) - radians(:lng))
            + sin(radians(:lat)) * sin(radians(r.centerLatitude))
        )) <= r.radiusKm
        ORDER BY (6371 * acos(
            cos(radians(:lat)) * cos(radians(r.centerLatitude))
            * cos(radians(r.centerLongitude) - radians(:lng))
            + sin(radians(:lat)) * sin(radians(r.centerLatitude))
        ))
    """)
    fun findNearbyRooms(lat: Double, lng: Double): List<ChatRoom>
}
