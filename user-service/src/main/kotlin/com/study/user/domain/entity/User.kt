package com.study.user.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var name: String,

    var password: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthProvider = AuthProvider.LOCAL,

    var providerId: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    fun updateName(newName: String) {
        this.name = newName
        this.updatedAt = LocalDateTime.now()
    }

    fun updateOAuth2Info(newName: String, newProviderId: String) {
        this.name = newName
        this.providerId = newProviderId
        this.updatedAt = LocalDateTime.now()
    }
}
