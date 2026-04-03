package com.study.notification.infrastructure.sse

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Component
class SseEmitterRegistry {

    private val log = LoggerFactory.getLogger(javaClass)
    private val emitters = ConcurrentHashMap<Long, SseEmitter>()

    fun register(userId: Long): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        emitters[userId] = emitter

        emitter.onCompletion { emitters.remove(userId) }
        emitter.onTimeout { emitters.remove(userId) }
        emitter.onError { emitters.remove(userId) }

        return emitter
    }

    fun send(userId: Long, eventName: String, data: Any) {
        val emitter = emitters[userId] ?: return
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data))
        } catch (e: Exception) {
            log.warn("Failed to send SSE to user {}", userId)
            emitters.remove(userId)
        }
    }

    fun isConnected(userId: Long): Boolean = emitters.containsKey(userId)
}
