package com.mesalive.service.validator

import com.mesalive.domain.Mesa
import java.time.OffsetDateTime

interface DisponibilidadeValidator {
    /** Valida a alocação da mesa no intervalo [dataHora, dataHoraFim). */
    fun validar(mesa: Mesa, dataHora: OffsetDateTime, dataHoraFim: OffsetDateTime)
}
