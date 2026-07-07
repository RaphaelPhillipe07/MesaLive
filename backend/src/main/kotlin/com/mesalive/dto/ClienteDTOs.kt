package com.mesalive.dto

data class ClienteResponseDTO(
    val id: Long,
    val nome: String,
    val telefone: String,
    val email: String?
)
