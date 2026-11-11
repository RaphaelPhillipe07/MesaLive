package com.mesalive.domain

import jakarta.persistence.*

@Entity
@Table(name = "cliente")
class Cliente(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var nome: String,

    @Column(nullable = false)
    val telefone: String,

    @Column
    var email: String? = null
)
