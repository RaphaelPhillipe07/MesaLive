package com.mesalive.repository

import com.mesalive.domain.Cliente
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClienteRepository : JpaRepository<Cliente, Long> {
    fun findByTelefone(telefone: String): Cliente?
}
