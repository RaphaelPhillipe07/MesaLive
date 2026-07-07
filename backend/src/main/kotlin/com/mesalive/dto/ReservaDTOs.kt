package com.mesalive.dto

import com.mesalive.domain.StatusReserva
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class ReservaRequestDTO(
    @field:NotNull(message = "O ID da mesa é obrigatório.")
    val mesaId: Long?,

    @field:NotBlank(message = "O nome do cliente é obrigatório.")
    val nomeCliente: String?,

    @field:NotBlank(message = "O telefone de contato é obrigatório.")
    val telefoneCliente: String?,

    val emailCliente: String?,

    @field:NotNull(message = "A data e hora da reserva são obrigatórias.")
    @field:Future(message = "A data e hora da reserva devem estar no futuro.")
    val dataHora: OffsetDateTime?,

    @field:NotNull(message = "A quantidade de pessoas é obrigatória.")
    @field:Min(value = 1, message = "A reserva deve ser para pelo menos 1 pessoa.")
    val quantidadePessoas: Int?
)

data class ReservaResponseDTO(
    val id: Long,
    val mesa: MesaResponseDTO,
    val cliente: ClienteResponseDTO,
    val dataHora: OffsetDateTime,
    val quantidadePessoas: Int,
    val status: StatusReserva
)

data class ReservaStatusRequestDTO(
    @field:NotNull(message = "O status da reserva é obrigatório.")
    val status: StatusReserva?
)
