package com.mesalive.service

import com.mesalive.dto.MesaPainelResponseDTO

interface SalonService {
    /** Mapa do salão com o status atual de cada mesa. */
    fun listarMesasComStatus(): List<MesaPainelResponseDTO>
}
