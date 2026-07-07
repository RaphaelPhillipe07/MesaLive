package com.mesalive.domain

import jakarta.persistence.*

@Entity
@Table(name = "mesa")
class Mesa(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val numero: String,

    @Column(nullable = false)
    val capacidade: Int,

    @Column(nullable = false)
    var ativo: Boolean = true
)
