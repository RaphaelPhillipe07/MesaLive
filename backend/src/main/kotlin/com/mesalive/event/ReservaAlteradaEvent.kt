package com.mesalive.event

import com.mesalive.domain.Reserva
import org.springframework.context.ApplicationEvent

class ReservaAlteradaEvent(
    val reserva: Reserva
) : ApplicationEvent(reserva)
