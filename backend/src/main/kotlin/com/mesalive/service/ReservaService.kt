package com.mesalive.service

import com.mesalive.domain.Reserva
import com.mesalive.domain.StatusReserva
import java.time.OffsetDateTime

interface ReservaService {
    fun criarReserva(
        mesaId: Long,
        nomeCliente: String,
        telefoneCliente: String,
        emailCliente: String?,
        dataHora: OffsetDateTime,
        quantidadePessoas: Int
    ): Reserva

    fun buscarPorId(id: Long): Reserva
    fun buscarPorIdETelefone(id: Long, telefone: String): Reserva
    fun cancelarReserva(id: Long, telefone: String): Reserva
    fun atualizarStatus(id: Long, status: StatusReserva): Reserva
    fun listarTodas(): List<Reserva>
}
