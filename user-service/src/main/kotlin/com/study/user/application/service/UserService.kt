package com.study.user.application.service

import com.study.common.event.DomainEvent
import com.study.common.event.UserCreatedEvent
import com.study.common.exception.EntityNotFoundException
import com.study.user.application.dto.CreateUserRequest
import com.study.user.application.dto.UserResponse
import com.study.user.domain.entity.User
import com.study.user.domain.repository.UserRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val kafkaTemplate: KafkaTemplate<String, DomainEvent>
) {
    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        val user = User(
            email = request.email,
            name = request.name,
            password = request.password
        )
        val saved = userRepository.save(user)
        kafkaTemplate.send("user-events", UserCreatedEvent(saved.id, saved.email))
        return UserResponse.from(saved)
    }

    fun getUser(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User", id) }
        return UserResponse.from(user)
    }
}
