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
        val clienteExistente = clienteRepository.findByTelefone(telefone)
        if (clienteExistente != null) {
             val clienteAtualizado = Cliente(
                id = clienteExistente.id,
                nome = nome,
                telefone = telefone,
                email = email ?: clienteExistente.email
            )
            return clienteRepository.save(clienteAtualizado)
        }
        val novoCliente = Cliente(
            nome = nome,
            telefone = telefone,
            email = email
        )
        return clienteRepository.save(novoCliente)
    }
}
