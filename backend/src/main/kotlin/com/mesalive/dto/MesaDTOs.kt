package com.mesalive.dto

data class MesaResponseDTO(
    val id: Long,
    val numero: String,
    val capacidade: Int,
    val ativo: Boolean
)

data class MesaStatusRequestDTO(
    val ativo: Boolean
)
