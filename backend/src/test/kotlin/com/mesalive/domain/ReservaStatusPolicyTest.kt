package com.mesalive.domain

import com.mesalive.exception.TransicaoInvalidaException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReservaStatusPolicyTest {

    @Test
    fun `confirmada pode ir para check-in, no-show ou cancelada`() {
        assertTrue(ReservaStatusPolicy.podeTransicionar(StatusReserva.CONFIRMADA, StatusReserva.CLIENTE_CHEGOU))
        assertTrue(ReservaStatusPolicy.podeTransicionar(StatusReserva.CONFIRMADA, StatusReserva.NO_SHOW))
        assertTrue(ReservaStatusPolicy.podeTransicionar(StatusReserva.CONFIRMADA, StatusReserva.CANCELADA))
    }

    @Test
    fun `cliente sentado e no-show so podem ser liberados`() {
        assertTrue(ReservaStatusPolicy.podeTransicionar(StatusReserva.CLIENTE_CHEGOU, StatusReserva.CANCELADA))
        assertTrue(ReservaStatusPolicy.podeTransicionar(StatusReserva.NO_SHOW, StatusReserva.CANCELADA))

        assertFalse(ReservaStatusPolicy.podeTransicionar(StatusReserva.CLIENTE_CHEGOU, StatusReserva.CONFIRMADA))
        assertFalse(ReservaStatusPolicy.podeTransicionar(StatusReserva.CLIENTE_CHEGOU, StatusReserva.NO_SHOW))
        assertFalse(ReservaStatusPolicy.podeTransicionar(StatusReserva.NO_SHOW, StatusReserva.CLIENTE_CHEGOU))
    }

    @Test
    fun `cancelada e terminal`() {
        StatusReserva.entries
            .filter { it != StatusReserva.CANCELADA }
            .forEach { destino ->
                assertFalse(
                    ReservaStatusPolicy.podeTransicionar(StatusReserva.CANCELADA, destino),
                    "cancelada não deveria ir para $destino"
                )
            }
    }

    @Test
    fun `repetir o status atual e permitido para PATCH idempotente`() {
        StatusReserva.entries.forEach { status ->
            assertTrue(ReservaStatusPolicy.podeTransicionar(status, status))
        }
    }

    @Test
    fun `validar lanca excecao em transicao invalida`() {
        assertThrows<TransicaoInvalidaException> {
            ReservaStatusPolicy.validar(StatusReserva.CANCELADA, StatusReserva.CLIENTE_CHEGOU)
        }
    }
}
