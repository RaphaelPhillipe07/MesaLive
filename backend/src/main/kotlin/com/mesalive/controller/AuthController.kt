package com.mesalive.controller

import com.mesalive.dto.LoginRequestDTO
import com.mesalive.dto.LoginResponseDTO
import com.mesalive.exception.CredenciaisInvalidasException
import com.mesalive.repository.UsuarioRepository
import com.mesalive.security.JwtService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequestDTO): ResponseEntity<LoginResponseDTO> {
        val usuario = usuarioRepository.findByEmail(request.email!!)
            ?: throw CredenciaisInvalidasException()

        if (!passwordEncoder.matches(request.senha!!, usuario.senhaHash)) {
            throw CredenciaisInvalidasException()
        }

        val token = jwtService.gerarToken(usuario.email, usuario.role.name)

        return ResponseEntity.ok(
            LoginResponseDTO(
                token = token,
                nome = usuario.nome,
                email = usuario.email,
                role = usuario.role.name
            )
        )
    }
}
