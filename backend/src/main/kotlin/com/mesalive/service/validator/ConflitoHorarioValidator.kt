package com.mesalive.service.validator

import com.mesalive.domain.Mesa
import com.mesalive.domain.StatusReserva
import com.mesalive.exception.ConflitoReservaException
import com.mesalive.repository.ReservaRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * Verificação antecipada (mensagem amigável) de sobreposição na mesa.
 * A garantia final é o constraint `ex_reserva_sem_sobreposicao` no PostgreSQL.
 */
@Component
class ConflitoHorarioValidator(
    private val reservaRepository: ReservaRepository
) : DisponibilidadeValidator {

    override fun validar(mesa: Mesa, dataHora: OffsetDateTime, dataHoraFim: OffsetDateTime) {
        val mesaId = mesa.id ?: return

        val sobreposicoes = reservaRepository.countSobreposicoes(
            mesaId = mesaId,
            statuses = STATUSES_QUE_OCUPAM_MESA,
            inicio = dataHora,
            fim = dataHoraFim
        )

        if (sobreposicoes > 0) {
            throw ConflitoReservaException.paraMesa(mesaId, dataHora)
        }
    }

    private companion object {
        /** Reserva cancelada não ocupa mesa; cliente já sentado ocupa. */
        val STATUSES_QUE_OCUPAM_MESA = listOf(StatusReserva.CONFIRMADA, StatusReserva.CLIENTE_CHEGOU)
    }
}
