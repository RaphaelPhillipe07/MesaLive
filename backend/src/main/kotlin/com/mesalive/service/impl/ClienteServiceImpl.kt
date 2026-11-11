package com.mesalive.service.impl

import com.mesalive.domain.Cliente
import com.mesalive.exception.NegocioException
import com.mesalive.repository.ClienteRepository
import com.mesalive.service.ClienteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClienteServiceImpl(
    private val clienteRepository: ClienteRepository
) : ClienteService {

    @Transactional(readOnly = true)
    override fun buscarPorId(id: Long): Cliente {
        return clienteRepository.findById(id).orElseThrow { NegocioException("Cliente com ID $id não encontrado.") }
    }

    @Transactional
    override fun buscarOuCriarCliente(nome: String, telefone: String, email: String?): Cliente {
        // Telefone é a identidade do cliente (garantido por uq_cliente_telefone).
        val clienteExistente = clienteRepository.findByTelefone(telefone)
        if (clienteExistente != null) {
            clienteExistente.nome = nome
            if (!email.isNullOrBlank()) {
                clienteExistente.email = email
            }
            return clienteExistente
        }

        return clienteRepository.save(
            Cliente(nome = nome, telefone = telefone, email = email)
        )
    }
}
