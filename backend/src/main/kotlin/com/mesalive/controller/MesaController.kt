package com.mesalive.controller

import com.mesalive.dto.MesaPainelResponseDTO
import com.mesalive.dto.MesaStatusRequestDTO
import com.mesalive.domain.StatusReserva
import com.mesalive.service.MesaService
import com.mesalive.service.ReservaService
import com.mesalive.dto.MesaResponseDTO
import com.mesalive.mapper.toDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/mesas")
class MesaController(
    private val mesaService: MesaService,
    private val reservaService: ReservaService
) {

    @GetMapping("/publicas")
    fun listarMesasPublicas(): ResponseEntity<List<MesaResponseDTO>> {
        val mesas = mesaService.listarAtivas().map { it.toDTO() }
        return ResponseEntity.ok(mesas)
    }

    @GetMapping
    fun listarMesasComStatus(): ResponseEntity<List<MesaPainelResponseDTO>> {
        val mesas = mesaService.listarTodas()
        val reservas = reservaService.listarTodas()
        val agora = OffsetDateTime.now()

        val response = mesas.map { mesa ->
            // Procurar se há reserva ativa/relevante agora para essa mesa
            val reservaAtiva = reservas.filter { r -> r.mesa.id == mesa.id }
                .firstOrNull { r ->
                    when (r.status) {
                        StatusReserva.CLIENTE_CHEGOU -> true // Ocupada
                        StatusReserva.NO_SHOW -> {
                            // Se foi marcado como no-show hoje, exibe no-show
                            r.dataHora.toLocalDate() == agora.toLocalDate()
                        }
                        StatusReserva.CONFIRMADA -> {
                            // Se a reserva é hoje e estamos na janela da reserva (30min antes até 90min depois)
                            val inicioJanela = r.dataHora.minusMinutes(30)
                            val fimJanela = r.dataHora.plusMinutes(90)
                            agora.isAfter(inicioJanela) && agora.isBefore(fimJanela)
                        }
                        StatusReserva.CANCELADA -> false
                    }
                }

            val statusAtual = when {
                !mesa.ativo -> "INATIVA"
                reservaAtiva == null -> "LIVRE"
                reservaAtiva.status == StatusReserva.CLIENTE_CHEGOU -> "OCUPADA"
                reservaAtiva.status == StatusReserva.NO_SHOW -> "NO_SHOW"
                reservaAtiva.status == StatusReserva.CONFIRMADA -> "RESERVADA"
                else -> "LIVRE"
            }

            MesaPainelResponseDTO(
                id = mesa.id ?: 0L,
                numero = mesa.numero,
                capacidade = mesa.capacidade,
                ativo = mesa.ativo,
                statusAtual = statusAtual,
                reservaAtivaId = reservaAtiva?.id,
                nomeCliente = reservaAtiva?.cliente?.nome,
                dataHoraReserva = reservaAtiva?.dataHora?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            )
        }

        return ResponseEntity.ok(response)
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
