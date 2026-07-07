package com.mesalive.service.validator

import com.mesalive.domain.Mesa
import java.time.OffsetDateTime

interface DisponibilidadeValidator {
    fun validar(mesa: Mesa, dataHora: OffsetDateTime)
}
