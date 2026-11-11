package com.mesalive.controller

import com.mesalive.dto.MesaPainelResponseDTO
import com.mesalive.dto.MesaResponseDTO
import com.mesalive.dto.MesaStatusRequestDTO
import com.mesalive.mapper.toDTO
import com.mesalive.service.MesaService
import com.mesalive.service.SalonService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/mesas")
class MesaController(
    private val mesaService: MesaService,
    private val salonService: SalonService
) {

    @GetMapping("/publicas")
    fun listarMesasPublicas(): ResponseEntity<List<MesaResponseDTO>> {
        val mesas = mesaService.listarAtivas().map { it.toDTO() }
        return ResponseEntity.ok(mesas)
    }

    @GetMapping
    fun listarMesasComStatus(): ResponseEntity<List<MesaPainelResponseDTO>> {
        return ResponseEntity.ok(salonService.listarMesasComStatus())
    }

    @PatchMapping("/{id}/status")
    fun atualizarStatus(
        @PathVariable id: Long,
        @RequestBody request: MesaStatusRequestDTO
    ): ResponseEntity<MesaPainelResponseDTO> {
        val mesa = mesaService.atualizarStatus(id, request.ativo)
        return ResponseEntity.ok(
            MesaPainelResponseDTO(
                id = mesa.id ?: 0L,
                numero = mesa.numero,
                capacidade = mesa.capacidade,
                ativo = mesa.ativo,
                statusAtual = if (mesa.ativo) "LIVRE" else "INATIVA"
            )
        )
    }
}
