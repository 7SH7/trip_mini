package com.study.common.event

data class ChatMessageEvent(
    val chatRoomId: Long = 0,
    val senderId: Long = 0,
    val content: String = "",
    val roomName: String = ""
) : DomainEvent()
