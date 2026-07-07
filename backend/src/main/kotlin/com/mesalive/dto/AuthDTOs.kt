package com.mesalive.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequestDTO(
    @field:NotBlank(message = "O e-mail é obrigatório.")
    @field:Email(message = "O e-mail informado é inválido.")
    val email: String?,

    @field:NotBlank(message = "A senha é obrigatória.")
    val senha: String?
)

data class LoginResponseDTO(
    val token: String,
    val nome: String,
    val email: String,
    val role: String
)
