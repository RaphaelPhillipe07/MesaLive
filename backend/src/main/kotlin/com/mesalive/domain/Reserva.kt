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

    /**
     * Fim do intervalo ocupado pela reserva. É uma coluna persistida (e não uma expressão
     * calculada) porque o constraint de exclusão do PostgreSQL exige expressão imutável.
     */
    @Column(name = "data_hora_fim", nullable = false)
    val dataHoraFim: OffsetDateTime = dataHora.plusMinutes(ReservaTempos.DURACAO_PADRAO_MINUTOS),

    @Column(name = "quantidade_pessoas", nullable = false)
    val quantidadePessoas: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StatusReserva = StatusReserva.CONFIRMADA,

    @Version
    @Column(nullable = false)
    var version: Long? = null,

    @Column(name = "criado_em", nullable = false)
    var criadoEm: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: OffsetDateTime = OffsetDateTime.now()
) {
    @PrePersist
    fun aoPersistir() {
        val agora = OffsetDateTime.now()
        criadoEm = agora
        atualizadoEm = agora
    }

    @PreUpdate
    fun aoAtualizar() {
        atualizadoEm = OffsetDateTime.now()
    }
}
