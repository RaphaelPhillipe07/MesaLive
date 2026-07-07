package com.mesalive.service.validator

import com.mesalive.domain.Mesa
import com.mesalive.domain.StatusReserva
import com.mesalive.exception.ConflitoReservaException
import com.mesalive.repository.ReservaRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class ConflitoHorarioValidator(
    private val reservaRepository: ReservaRepository
) : DisponibilidadeValidator {

    override fun validar(mesa: Mesa, dataHora: OffsetDateTime) {
        val mesaId = mesa.id ?: return
        val inicio = dataHora.minusMinutes(89)
        val fim = dataHora.plusMinutes(89)

        val reservasConflitantes = reservaRepository.findReservasConflitantes(
            mesaId = mesaId,
            status = StatusReserva.CONFIRMADA,
            inicio = inicio,
            fim = fim
        )

        if (reservasConflitantes.isNotEmpty()) {
            throw ConflitoReservaException.paraMesa(mesaId, dataHora)
        }
    }
}
