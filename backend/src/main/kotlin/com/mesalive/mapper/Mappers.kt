package com.mesalive.mapper

import com.mesalive.domain.Cliente
import com.mesalive.domain.Mesa
import com.mesalive.domain.Reserva
import com.mesalive.dto.ClienteResponseDTO
import com.mesalive.dto.MesaResponseDTO
import com.mesalive.dto.ReservaResponseDTO

fun Mesa.toDTO() = MesaResponseDTO(
    id = this.id ?: 0L,
    numero = this.numero,
    capacidade = this.capacidade,
    ativo = this.ativo
)

fun Cliente.toDTO() = ClienteResponseDTO(
    id = this.id ?: 0L,
    nome = this.nome,
    telefone = this.telefone,
    email = this.email
)

fun Reserva.toDTO() = ReservaResponseDTO(
    id = this.id ?: 0L,
    mesa = this.mesa.toDTO(),
    cliente = this.cliente.toDTO(),
    dataHora = this.dataHora,
    quantidadePessoas = this.quantidadePessoas,
    status = this.status
)
