package com.mesalive.service.validator

import com.mesalive.domain.Mesa
import com.mesalive.domain.ReservaTempos
import com.mesalive.domain.StatusReserva
import com.mesalive.exception.ConflitoReservaException
import com.mesalive.repository.ReservaRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ConflitoHorarioValidatorTest {

    @MockK
    private lateinit var reservaRepository: ReservaRepository

    private val validator by lazy { ConflitoHorarioValidator(reservaRepository) }
    private val mesa = Mesa(id = 7L, numero = "Mesa 7", capacidade = 4, ativo = true)
    private val inicio: OffsetDateTime = OffsetDateTime.parse("2026-12-24T20:00:00Z")
    private val fim: OffsetDateTime = inicio.plusMinutes(ReservaTempos.DURACAO_PADRAO_MINUTOS)

    @Test
    fun `deve rejeitar quando a mesa ja esta ocupada no intervalo`() {
        every { reservaRepository.countSobreposicoes(any(), any(), any(), any()) } returns 1

        assertThrows<ConflitoReservaException> {
            validator.validar(mesa, inicio, fim)
        }
    }

    @Test
    fun `deve aceitar quando nao ha sobreposicao`() {
        every { reservaRepository.countSobreposicoes(any(), any(), any(), any()) } returns 0

        validator.validar(mesa, inicio, fim)
    }

    @Test
    fun `deve considerar reservas confirmadas e cliente ja sentado`() {
        every { reservaRepository.countSobreposicoes(any(), any(), any(), any()) } returns 0

        validator.validar(mesa, inicio, fim)

        verify {
            reservaRepository.countSobreposicoes(
                mesaId = 7L,
                statuses = listOf(StatusReserva.CONFIRMADA, StatusReserva.CLIENTE_CHEGOU),
                inicio = inicio,
                fim = fim
            )
        }
    }

    @Test
    fun `mesa sem id (nao persistida) nao consulta o banco`() {
        validator.validar(Mesa(numero = "Nova ${UUID.randomUUID()}", capacidade = 2), inicio, fim)

        verify(exactly = 0) { reservaRepository.countSobreposicoes(any(), any(), any(), any()) }
    }
}
