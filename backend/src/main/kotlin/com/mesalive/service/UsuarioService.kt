package com.mesalive.service

import com.mesalive.domain.Usuario
import com.mesalive.dto.UsuarioRequestDTO

interface UsuarioService {
    fun cadastrar(dto: UsuarioRequestDTO): Usuario
}
