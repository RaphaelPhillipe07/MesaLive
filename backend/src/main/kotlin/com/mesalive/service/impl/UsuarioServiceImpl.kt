package com.mesalive.service.impl

import com.mesalive.domain.Usuario
import com.mesalive.dto.UsuarioRequestDTO
import com.mesalive.exception.NegocioException
import com.mesalive.repository.UsuarioRepository
import com.mesalive.service.UsuarioService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsuarioServiceImpl(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder
) : UsuarioService {

    @Transactional
    override fun cadastrar(dto: UsuarioRequestDTO): Usuario {
        val existente = usuarioRepository.findByEmail(dto.email!!)
        if (existente != null) {
            throw NegocioException("O e-mail ${dto.email} já está em uso no sistema.")
        }

        val senhaCriptografada = passwordEncoder.encode(dto.senha!!)
        val novoUsuario = Usuario(
            nome = dto.nome!!,
            email = dto.email,
            senhaHash = senhaCriptografada!!,
            role = dto.role!!
        )

        return usuarioRepository.save(novoUsuario)
    }
}
