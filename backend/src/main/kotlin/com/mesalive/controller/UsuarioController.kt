package com.mesalive.controller

import com.mesalive.dto.UsuarioRequestDTO
import com.mesalive.dto.UsuarioResponseDTO
import com.mesalive.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(private val usuarioService: UsuarioService) {

    @PostMapping
    fun cadastrar(@Valid @RequestBody req: UsuarioRequestDTO): ResponseEntity<UsuarioResponseDTO> {
        val u = usuarioService.cadastrar(req)
        val response = UsuarioResponseDTO(
            id = u.id ?: 0L,
            nome = u.nome,
            email = u.email,
            role = u.role
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
