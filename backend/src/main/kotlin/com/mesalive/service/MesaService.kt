package com.mesalive.service

import com.mesalive.domain.Mesa

interface MesaService {
    fun listarTodas(): List<Mesa>
    fun listarAtivas(): List<Mesa>
    fun buscarPorId(id: Long): Mesa
    fun buscarPorNumero(numero: String): Mesa?
    fun salvar(mesa: Mesa): Mesa
    fun atualizarStatus(id: Long, ativo: Boolean): Mesa
}
