package com.mesalive.service

import com.mesalive.domain.*
import com.mesalive.exception.*
import com.mesalive.event.ReservaAlteradaEvent
import com.mesalive.repository.ReservaRepository
import com.mesalive.service.impl.ReservaServiceImpl
import com.mesalive.service.validator.DisponibilidadeValidator
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ReservaServiceTest {

    private val reservaRepository = mockk<ReservaRepository>()
    private val mesaService = mockk<MesaService>()
    private val clienteService = mockk<ClienteService>()
    private val validator = mockk<DisponibilidadeValidator>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val reservaService = ReservaServiceImpl(
        reservaRepository,
        mesaService,
        clienteService,
        listOf(validator),
        eventPublisher
    )

    @Test
    fun `deve criar reserva com sucesso quando mesa esta disponivel e dados validos`() {

        val mesa = Mesa(id = 1L, numero = "Mesa 1", capacidade = 4, ativo = true)
        val cliente = Cliente(id = 1L, nome = "João", telefone = "11999999999")
        val dataHora = OffsetDateTime.now().plusDays(1)
        val reserva = Reserva(id = 1L, mesa = mesa, cliente = cliente, dataHora = dataHora, quantidadePessoas = 2)

        every { mesaService.buscarPorId(1L) } returns mesa
        every { validator.validar(mesa, dataHora) } just Runs
        every { clienteService.buscarOuCriarCliente("João", "11999999999", any()) } returns cliente
        every { reservaRepository.save(any<Reserva>()) } returns reserva

        val result = reservaService.criarReserva(
            mesaId = 1L,
            nomeCliente = "João",
            telefoneCliente = "11999999999",
            emailCliente = null,
            dataHora = dataHora,
            quantidadePessoas = 2
        )

        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("Mesa 1", result.mesa.numero)
        assertEquals("João", result.cliente.nome)
        verify { reservaRepository.save(any<Reserva>()) }
        verify { eventPublisher.publishEvent(any<ReservaAlteradaEvent>()) }
    }

    @Test
    fun `deve lancar MesaInativaException quando mesa nao esta ativa`() {

        val mesa = Mesa(id = 1L, numero = "Mesa 1", capacidade = 4, ativo = false)
        val dataHora = OffsetDateTime.now().plusDays(1)

        every { mesaService.buscarPorId(1L) } returns mesa

        assertThrows<MesaInativaException> {
            reservaService.criarReserva(
                mesaId = 1L,
                nomeCliente = "João",
                telefoneCliente = "11999999999",
                emailCliente = null,
                dataHora = dataHora,
                quantidadePessoas = 2
            )
        }
        verify(exactly = 0) { reservaRepository.save(any()) }
    }

    @Test
    fun `deve lancar CapacidadeMesaInsuficienteException quando quantidade de pessoas excede capacidade`() {

        val mesa = Mesa(id = 1L, numero = "Mesa 1", capacidade = 2, ativo = true)
        val dataHora = OffsetDateTime.now().plusDays(1)

        every { mesaService.buscarPorId(1L) } returns mesa

        assertThrows<CapacidadeMesaInsuficienteException> {
            reservaService.criarReserva(
                mesaId = 1L,
                nomeCliente = "João",
                telefoneCliente = "11999999999",
                emailCliente = null,
                dataHora = dataHora,
                quantidadePessoas = 4
            )
        }
        verify(exactly = 0) { reservaRepository.save(any()) }
    }
}
