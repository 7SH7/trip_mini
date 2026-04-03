package com.study.chat.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class OnlineUserTracker(
    private val redisTemplate: StringRedisTemplate
) {
    fun join(roomId: Long, userId: Long) {
        redisTemplate.opsForSet().add("chatroom:$roomId:online", userId.toString())
    }

    fun leave(roomId: Long, userId: Long) {
        redisTemplate.opsForSet().remove("chatroom:$roomId:online", userId.toString())
    }

    fun getOnlineUsers(roomId: Long): Set<Long> {
        return redisTemplate.opsForSet().members("chatroom:$roomId:online")
            ?.map { it.toLong() }?.toSet() ?: emptySet()
    }

    fun isOnline(roomId: Long, userId: Long): Boolean {
        return redisTemplate.opsForSet().isMember("chatroom:$roomId:online", userId.toString()) == true
    }
}
