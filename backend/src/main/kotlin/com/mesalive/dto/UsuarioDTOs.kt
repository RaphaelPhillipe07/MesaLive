package com.mesalive.dto

import com.mesalive.domain.Role
import jakarta.validation.constraints.*

data class UsuarioRequestDTO(
    @field:NotBlank(message = "Nome é obrigatório.")
    val nome: String?,

    @field:NotBlank(message = "E-mail é obrigatório.")
    @field:Email(message = "Formato de e-mail inválido.")
    val email: String?,

    @field:NotBlank(message = "Senha é obrigatória.")
    @field:Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    val senha: String?,

    @field:NotNull(message = "O papel (Role) do usuário é obrigatório.")
    val role: Role?
)

data class UsuarioResponseDTO(
    val id: Long,
    val nome: String,
    val email: String,
    val role: Role
)
