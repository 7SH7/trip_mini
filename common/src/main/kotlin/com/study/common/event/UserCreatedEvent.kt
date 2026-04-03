package com.study.common.event

data class UserCreatedEvent(
    val userId: Long = 0,
    val email: String = ""
) : DomainEvent()
