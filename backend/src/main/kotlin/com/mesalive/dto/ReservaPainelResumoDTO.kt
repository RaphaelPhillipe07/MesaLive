package com.mesalive.dto

import com.mesalive.domain.StatusReserva
import java.time.OffsetDateTime

/**
 * Leitura enxuta usada exclusivamente pelo mapa do salão: evita carregar entidades
 * e dispara um único SELECT (com join no cliente) em vez de uma consulta por mesa.
 */
data class ReservaPainelResumoDTO(
    val mesaId: Long,
    val reservaId: Long,
    val nomeCliente: String,
    val dataHora: OffsetDateTime,
    val status: StatusReserva
)
