package com.mesalive.service.impl

import com.mesalive.domain.Mesa
import com.mesalive.domain.ReservaTempos
import com.mesalive.domain.StatusReserva
import com.mesalive.dto.MesaPainelResponseDTO
import com.mesalive.dto.ReservaPainelResumoDTO
import com.mesalive.repository.ReservaRepository
import com.mesalive.service.MesaService
import com.mesalive.service.SalonService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Resolve o mapa do salão a partir de uma única consulta de reservas relevantes.
 * Antes essa regra vivia no controller e carregava todo o histórico em memória.
 */
@Service
class SalonServiceImpl(
    private val mesaService: MesaService,
    private val reservaRepository: ReservaRepository,
    private val clock: Clock
) : SalonService {

    @Transactional(readOnly = true)
    override fun listarMesasComStatus(): List<MesaPainelResponseDTO> {
        val agora = OffsetDateTime.now(clock)

        val reservasPorMesa = reservaRepository.findResumosParaPainel(
            inicio = agora.minusMinutes(ReservaTempos.TOLERANCIA_SAIDA_MINUTOS),
            fim = agora.plusMinutes(ReservaTempos.TOLERANCIA_ENTRADA_MINUTOS),
            statusesPermanentes = listOf(StatusReserva.CLIENTE_CHEGOU, StatusReserva.NO_SHOW),
            statusConfirmada = StatusReserva.CONFIRMADA
        ).groupBy { it.mesaId }

        return mesaService.listarTodas().map { mesa ->
            val reserva = mesasReservaDe(mesa, reservasPorMesa)
            MesaPainelResponseDTO(
                id = mesa.id ?: 0L,
                numero = mesa.numero,
                capacidade = mesa.capacidade,
                ativo = mesa.ativo,
                statusAtual = statusAtual(mesa, reserva),
                reservaAtivaId = reserva?.reservaId,
                nomeCliente = reserva?.nomeCliente,
                dataHoraReserva = reserva?.dataHora?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            )
        }
    }

    private fun mesasReservaDe(
        mesa: Mesa,
        reservasPorMesa: Map<Long, List<ReservaPainelResumoDTO>>
    ): ReservaPainelResumoDTO? {
        return mesa.id?.let { reservasPorMesa[it] }?.minWithOrNull(ORDEM_RELEVANCIA)
    }

    private fun statusAtual(mesa: Mesa, reserva: ReservaPainelResumoDTO?): String = when {
        !mesa.ativo -> "INATIVA"
        reserva == null -> "LIVRE"
        reserva.status == StatusReserva.CLIENTE_CHEGOU -> "OCUPADA"
        reserva.status == StatusReserva.NO_SHOW -> "NO_SHOW"
        else -> "RESERVADA"
    }

    private companion object {
        /** Mesa ocupada tem prioridade sobre no-show, que tem prioridade sobre reserva futura. */
        val ORDEM_RELEVANCIA: Comparator<ReservaPainelResumoDTO> =
            compareBy<ReservaPainelResumoDTO>({ prioridade(it.status) }, { it.dataHora })

        fun prioridade(status: StatusReserva): Int = when (status) {
            StatusReserva.CLIENTE_CHEGOU -> 0
            StatusReserva.NO_SHOW -> 1
            StatusReserva.CONFIRMADA -> 2
            StatusReserva.CANCELADA -> 3
        }
    }
}
