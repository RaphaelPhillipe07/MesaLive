package com.mesalive.domain

import jakarta.persistence.*

enum class Role {
    GARCOM,
    GERENTE
}

@Entity
@Table(name = "usuario")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val nome: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(name = "senha_hash", nullable = false)
    val senhaHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role
)
