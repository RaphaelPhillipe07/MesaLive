package com.mesalive.repository

import com.mesalive.domain.Mesa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MesaRepository : JpaRepository<Mesa, Long> {
    fun findByNumero(numero: String): Mesa?
}
