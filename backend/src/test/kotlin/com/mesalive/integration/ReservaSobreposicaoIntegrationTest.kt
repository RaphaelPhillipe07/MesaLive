package com.mesalive.integration

import com.mesalive.domain.Mesa
import com.mesalive.exception.ConflitoReservaException
import com.mesalive.repository.MesaRepository
import com.mesalive.service.ReservaService
import com.mesalive.support.EmbeddedPostgresSupport
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals

/**
 * Prova, contra um PostgreSQL real, que o double booking é impedido pelo banco
 * (constraint ex_reserva_sem_sobreposicao) e não apenas pela validação em código.
 */
@SpringBootTest
class ReservaSobreposicaoIntegrationTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun datasource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { EmbeddedPostgresSupport.jdbcUrl }
            registry.add("spring.datasource.username") { EmbeddedPostgresSupport.USERNAME }
            registry.add("spring.datasource.password") { EmbeddedPostgresSupport.PASSWORD }
        }
    }

    @Autowired
    private lateinit var reservaService: ReservaService

    @Autowired
    private lateinit var mesaRepository: MesaRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private fun novaMesa(): Long = mesaRepository.saveAndFlush(
        Mesa(numero = "T-${UUID.randomUUID()}", capacidade = 4)
    ).id!!

    private fun novoCliente(): Long {
        val telefone = "11" + abs(Random.nextInt(100_000_000, 999_999_999))
        return jdbcTemplate.queryForObject(
            "INSERT INTO cliente (nome, telefone) VALUES ('Cliente Teste', ?) RETURNING id",
            Long::class.java,
            telefone
        )!!
    }

    private fun inserirReserva(
        mesaId: Long,
        clienteId: Long,
        inicio: OffsetDateTime,
        status: String = "CONFIRMADA"
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO reserva (mesa_id, cliente_id, data_hora, data_hora_fim, quantidade_pessoas, status)
            VALUES (?, ?, ?, ?, 2, ?)
            """.trimIndent(),
            mesaId,
            clienteId,
            inicio,
            inicio.plusMinutes(90),
            status
        )
    }

    @Test
    fun `banco rejeita duas reservas sobrepostas na mesma mesa`() {
        val mesaId = novaMesa()
        val clienteId = novoCliente()
        val dezenove = OffsetDateTime.parse("2026-12-24T19:00:00Z")

        inserirReserva(mesaId, clienteId, dezenove)

        assertThrows<DataIntegrityViolationException> {
            inserirReserva(mesaId, clienteId, dezenove.plusMinutes(30))
        }
    }

    @Test
    fun `reservas encostadas no fim do intervalo nao se sobrepoem`() {
        val mesaId = novaMesa()
        val clienteId = novoCliente()
        val dezoito = OffsetDateTime.parse("2026-12-24T18:00:00Z")

        inserirReserva(mesaId, clienteId, dezoito)
        inserirReserva(mesaId, clienteId, dezoito.plusMinutes(90))

        val total = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reserva WHERE mesa_id = ?",
            Long::class.java,
            mesaId
        )
        assertEquals(2L, total)
    }

    @Test
    fun `reserva cancelada nao bloqueia o horario`() {
        val mesaId = novaMesa()
        val clienteId = novoCliente()
        val dezenove = OffsetDateTime.parse("2026-12-24T19:00:00Z")

        inserirReserva(mesaId, clienteId, dezenove, status = "CANCELADA")
        inserirReserva(mesaId, clienteId, dezenove.plusMinutes(30))

        val ativas = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reserva WHERE mesa_id = ? AND status = 'CONFIRMADA'",
            Long::class.java,
            mesaId
        )
        assertEquals(1L, ativas)
    }

    @Test
    fun `segunda reserva no mesmo horario falha pelo servico com conflito`() {
        val mesaId = novaMesa()
        val telefone = "11" + abs(Random.nextInt(100_000_000, 999_999_999))
        val inicio = OffsetDateTime.parse("2030-06-01T19:00:00Z")

        reservaService.criarReserva(
            mesaId = mesaId,
            nomeCliente = "Ana",
            telefoneCliente = telefone,
            emailCliente = null,
            dataHora = inicio,
            quantidadePessoas = 2
        )

        assertThrows<ConflitoReservaException> {
            reservaService.criarReserva(
                mesaId = mesaId,
                nomeCliente = "Bruno",
                telefoneCliente = "11" + abs(Random.nextInt(100_000_000, 999_999_999)),
                emailCliente = null,
                dataHora = inicio.plusMinutes(30),
                quantidadePessoas = 2
            )
        }
    }
}
