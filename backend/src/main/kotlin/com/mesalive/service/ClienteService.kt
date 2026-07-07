package com.mesalive.service

import com.mesalive.domain.Cliente

interface ClienteService {
    fun buscarPorId(id: Long): Cliente
    fun buscarOuCriarCliente(nome: String, telefone: String, email: String?): Cliente
}
