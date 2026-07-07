package com.mesalive.websocket

import com.mesalive.event.ReservaAlteradaEvent
import com.mesalive.mapper.toDTO
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class ReservaEventListener(
    private val messagingTemplate: SimpMessagingTemplate
) {

    @EventListener
    fun handleReservaAlterada(event: ReservaAlteradaEvent) {
        val dto = event.reserva.toDTO()
        messagingTemplate.convertAndSend("/topic/salao", dto)
    }
}
