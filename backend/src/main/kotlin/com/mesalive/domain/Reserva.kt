package com.mesalive.domain

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class StatusReserva {
    CONFIRMADA,
    CANCELADA,
    CLIENTE_CHEGOU,
    NO_SHOW
}

@Entity
@Table(name = "reserva")
class Reserva(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id", nullable = false)
    val mesa: Mesa,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    val cliente: Cliente,

    @Column(name = "data_hora", nullable = false)
    val dataHora: OffsetDateTime,

    @Column(name = "quantidade_pessoas", nullable = false)
    val quantidadePessoas: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StatusReserva = StatusReserva.CONFIRMADA
)
