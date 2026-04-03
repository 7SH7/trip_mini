package com.study.chat.domain.repository

import com.study.chat.domain.entity.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByChatRoomIdOrderBySentAtDesc(chatRoomId: Long, pageable: Pageable): Page<ChatMessage>
}
