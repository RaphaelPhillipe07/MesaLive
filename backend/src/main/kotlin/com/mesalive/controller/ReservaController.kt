package com.mesalive.controller

import com.mesalive.dto.ReservaRequestDTO
import com.mesalive.dto.ReservaResponseDTO
import com.mesalive.dto.ReservaStatusRequestDTO
import com.mesalive.mapper.toDTO
import com.mesalive.service.ReservaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservas")
class ReservaController(
    private val reservaService: ReservaService
) {

    @PostMapping
    fun criarReserva(@Valid @RequestBody request: ReservaRequestDTO): ResponseEntity<ReservaResponseDTO> {
        val reserva = reservaService.criarReserva(
            mesaId = request.mesaId!!,
            nomeCliente = request.nomeCliente!!,
            telefoneCliente = request.telefoneCliente!!,
            emailCliente = request.emailCliente,
            dataHora = request.dataHora!!,
            quantidadePessoas = request.quantidadePessoas!!
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva.toDTO())
    }

    @GetMapping("/{id}")
    fun consultarReserva(
        @PathVariable id: Long,
        @RequestParam telefone: String
    ): ResponseEntity<ReservaResponseDTO> {
        val reserva = reservaService.buscarPorIdETelefone(id, telefone)
        return ResponseEntity.ok(reserva.toDTO())
    }

    @DeleteMapping("/{id}")
    fun cancelarReserva(
        @PathVariable id: Long,
        @RequestParam telefone: String
    ): ResponseEntity<ReservaResponseDTO> {
        val reserva = reservaService.cancelarReserva(id, telefone)
        return ResponseEntity.ok(reserva.toDTO())
    }

    @PatchMapping("/{id}/status")
    fun atualizarStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: ReservaStatusRequestDTO
    ): ResponseEntity<ReservaResponseDTO> {
        val reserva = reservaService.atualizarStatus(id, request.status!!)
        return ResponseEntity.ok(reserva.toDTO())
    }

    @GetMapping
    fun listarTodas(): ResponseEntity<List<ReservaResponseDTO>> {
        val reservas = reservaService.listarTodas().map { it.toDTO() }
        return ResponseEntity.ok(reservas)
    }
}
