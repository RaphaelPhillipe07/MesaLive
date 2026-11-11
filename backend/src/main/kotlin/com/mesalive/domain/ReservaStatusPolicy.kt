package com.mesalive.domain

import com.mesalive.exception.TransicaoInvalidaException

/**
 * Máquina de estados da reserva. Só a staff muda o status, e apenas pelos caminhos abaixo:
 *
 * CONFIRMADA ──► CLIENTE_CHEGOU ──► CANCELADA (liberar mesa)
 *            └─► NO_SHOW ─────────► CANCELADA (liberar mesa)
 *            └─► CANCELADA
 *
 * CANCELADA é terminal. Repetir o status atual é permitido (PATCH idempotente).
 */
object ReservaStatusPolicy {

    private val transicoes: Map<StatusReserva, Set<StatusReserva>> = mapOf(
        StatusReserva.CONFIRMADA to setOf(
            StatusReserva.CLIENTE_CHEGOU,
            StatusReserva.NO_SHOW,
            StatusReserva.CANCELADA
        ),
        StatusReserva.CLIENTE_CHEGOU to setOf(StatusReserva.CANCELADA),
        StatusReserva.NO_SHOW to setOf(StatusReserva.CANCELADA),
        StatusReserva.CANCELADA to emptySet()
    )

    fun podeTransicionar(de: StatusReserva, para: StatusReserva): Boolean {
        return de == para || transicoes[de].orEmpty().contains(para)
    }

    fun validar(de: StatusReserva, para: StatusReserva) {
        if (!podeTransicionar(de, para)) {
            throw TransicaoInvalidaException(de, para)
        }
    }
}
