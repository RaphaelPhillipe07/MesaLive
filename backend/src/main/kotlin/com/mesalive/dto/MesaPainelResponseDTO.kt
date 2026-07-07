package com.mesalive.dto

data class MesaPainelResponseDTO(
    val id: Long,
    val numero: String,
    val capacidade: Int,
    val ativo: Boolean,
    val statusAtual: String,
    val reservaAtivaId: Long? = null,
    val nomeCliente: String? = null,
    val dataHoraReserva: String? = null
)
