package com.mesalive.service.impl

import com.mesalive.domain.Mesa
import com.mesalive.exception.MesaNaoEncontradaException
import com.mesalive.repository.MesaRepository
import com.mesalive.service.MesaService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MesaServiceImpl(
    private val mesaRepository: MesaRepository
) : MesaService {

    @Transactional(readOnly = true)
    override fun listarTodas(): List<Mesa> {
        return mesaRepository.findAll()
    }

    @Transactional(readOnly = true)
    override fun listarAtivas(): List<Mesa> {
        return mesaRepository.findAll().filter { it.ativo }
    }

    @Transactional(readOnly = true)
    override fun buscarPorId(id: Long): Mesa {
        return mesaRepository.findById(id).orElseThrow { MesaNaoEncontradaException(id) }
    }

    @Transactional(readOnly = true)
    override fun buscarPorNumero(numero: String): Mesa? {
        return mesaRepository.findByNumero(numero)
    }

    @Transactional
    override fun salvar(mesa: Mesa): Mesa {
        return mesaRepository.save(mesa)
    }

    @Transactional
    override fun atualizarStatus(id: Long, ativo: Boolean): Mesa {
        val mesa = buscarPorId(id)
        mesa.ativo = ativo
        return mesaRepository.save(mesa)
    }
}
