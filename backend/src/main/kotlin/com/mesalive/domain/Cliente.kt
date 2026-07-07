package com.mesalive.domain

import jakarta.persistence.*

@Entity
@Table(name = "cliente")
class Cliente(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val nome: String,

    @Column(nullable = false)
    val telefone: String,

    @Column
    val email: String? = null
)
