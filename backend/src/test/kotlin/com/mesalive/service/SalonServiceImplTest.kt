package com.mesalive.service

import com.mesalive.domain.Mesa
import com.mesalive.domain.ReservaTempos
import com.mesalive.domain.StatusReserva
import com.mesalive.dto.ReservaPainelResumoDTO
import com.mesalive.repository.ReservaRepository
import com.mesalive.service.impl.SalonServiceImpl
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class SalonServiceImplTest {

    @MockK
    private lateinit var mesaService: MesaService

    @MockK
    private lateinit var reservaRepository: ReservaRepository

    private val agora = OffsetDateTime.parse("2026-11-11T20:00:00Z")
    private val clock = Clock.fixed(Instant.parse("2026-11-11T20:00:00Z"), ZoneOffset.UTC)

    private val salonService by lazy { SalonServiceImpl(mesaService, reservaRepository, clock) }

    private fun mesa(id: Long, ativo: Boolean = true) =
        Mesa(id = id, numero = "Mesa $id", capacidade = 4, ativo = ativo)

    private fun resumo(mesaId: Long, reservaId: Long, status: StatusReserva, hora: OffsetDateTime = agora) =
        ReservaPainelResumoDTO(
            mesaId = mesaId,
            reservaId = reservaId,
            nomeCliente = "Cliente $reservaId",
            dataHora = hora,
            status = status
        )

    private fun montarCenario(mesas: List<Mesa>, reservas: List<ReservaPainelResumoDTO>) {
        every { mesaService.listarTodas() } returns mesas
        every { reservaRepository.findResumosParaPainel(any(), any(), any(), any()) } returns reservas
    }

    @Test
    fun `mesa sem reserva aparece livre`() {
        montarCenario(listOf(mesa(1)), emptyList())

        val resultado = salonService.listarMesasComStatus()

        assertEquals("LIVRE", resultado.single().statusAtual)
        assertNull(resultado.single().reservaAtivaId)
    }

    @Test
    fun `cliente sentado tem prioridade sobre reserva futura na mesma mesa`() {
        montarCenario(
            mesas = listOf(mesa(1)),
            reservas = listOf(
                resumo(1, 10, StatusReserva.CONFIRMADA, agora.plusMinutes(ReservaTempos.TOLERANCIA_ENTRADA_MINUTOS)),
                resumo(1, 11, StatusReserva.CLIENTE_CHEGOU, agora.minusMinutes(20))
            )
        )

        val resultado = salonService.listarMesasComStatus().single()

        assertEquals("OCUPADA", resultado.statusAtual)
        assertEquals(11L, resultado.reservaAtivaId)
        assertEquals("Cliente 11", resultado.nomeCliente)
    }

    @Test
    fun `reserva confirmada na janela aparece como reservada`() {
        montarCenario(
            mesas = listOf(mesa(1)),
            reservas = listOf(resumo(1, 10, StatusReserva.CONFIRMADA, agora.plusMinutes(ReservaTempos.TOLERANCIA_ENTRADA_MINUTOS)))
        )

        assertEquals("RESERVADA", salonService.listarMesasComStatus().single().statusAtual)
    }

    @Test
    fun `no-show aparece como no show`() {
        montarCenario(
            mesas = listOf(mesa(1)),
            reservas = listOf(resumo(1, 10, StatusReserva.NO_SHOW, agora.minusMinutes(60)))
        )

        assertEquals("NO_SHOW", salonService.listarMesasComStatus().single().statusAtual)
    }

    @Test
    fun `mesa inativa aparece como inativa mesmo com reserva`() {
        montarCenario(
            mesas = listOf(mesa(1, ativo = false)),
            reservas = listOf(resumo(1, 10, StatusReserva.CLIENTE_CHEGOU))
        )

        assertEquals("INATIVA", salonService.listarMesasComStatus().single().statusAtual)
    }

    @Test
    fun `consulta o banco com a janela de tolerancia do salao`() {
        val inicio = slot<OffsetDateTime>()
        val fim = slot<OffsetDateTime>()
        every { mesaService.listarTodas() } returns emptyList()
        every {
            reservaRepository.findResumosParaPainel(capture(inicio), capture(fim), any(), any())
        } returns emptyList()

        salonService.listarMesasComStatus()

        assertEquals(agora.minusMinutes(ReservaTempos.TOLERANCIA_SAIDA_MINUTOS), inicio.captured)
        assertEquals(agora.plusMinutes(ReservaTempos.TOLERANCIA_ENTRADA_MINUTOS), fim.captured)
    }
}
