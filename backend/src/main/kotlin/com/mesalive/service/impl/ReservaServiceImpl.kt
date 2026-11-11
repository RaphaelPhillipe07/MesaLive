package com.mesalive.service.impl

import com.mesalive.domain.Reserva
import com.mesalive.domain.ReservaStatusPolicy
import com.mesalive.domain.ReservaTempos
import com.mesalive.domain.StatusReserva
import com.mesalive.event.ReservaAlteradaEvent
import com.mesalive.exception.*
import com.mesalive.repository.ReservaRepository
import com.mesalive.service.ClienteService
import com.mesalive.service.MesaService
import com.mesalive.service.ReservaService
import com.mesalive.service.validator.DisponibilidadeValidator
import org.hibernate.exception.ConstraintViolationException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class ReservaServiceImpl(
    private val reservaRepository: ReservaRepository,
    private val mesaService: MesaService,
    private val clienteService: ClienteService,
    private val validators: List<DisponibilidadeValidator>,
    private val eventPublisher: ApplicationEventPublisher
) : ReservaService {

    @Transactional
    override fun criarReserva(
        mesaId: Long,
        nomeCliente: String,
        telefoneCliente: String,
        emailCliente: String?,
        dataHora: OffsetDateTime,
        quantidadePessoas: Int
    ): Reserva {
        val mesa = mesaService.buscarPorId(mesaId)

        if (!mesa.ativo) {
            throw MesaInativaException(mesaId)
        }

        if (quantidadePessoas > mesa.capacidade) {
            throw CapacidadeMesaInsuficienteException(mesaId, mesa.capacidade, quantidadePessoas)
        }

        val dataHoraFim = dataHora.plusMinutes(ReservaTempos.DURACAO_PADRAO_MINUTOS)
        validators.forEach { it.validar(mesa, dataHora, dataHoraFim) }

        val cliente = clienteService.buscarOuCriarCliente(nomeCliente, telefoneCliente, emailCliente)

        val reserva = Reserva(
            mesa = mesa,
            cliente = cliente,
            dataHora = dataHora,
            dataHoraFim = dataHoraFim,
            quantidadePessoas = quantidadePessoas,
            status = StatusReserva.CONFIRMADA
        )

        val reservaSalva = salvarComProtecaoContraSobreposicao(reserva, mesaId, dataHora)

        eventPublisher.publishEvent(ReservaAlteradaEvent(reservaSalva))

        return reservaSalva
    }

    @Transactional(readOnly = true)
    override fun buscarPorId(id: Long): Reserva {
        return reservaRepository.findById(id).orElseThrow { ReservaNaoEncontradaException(id) }
    }

    @Transactional(readOnly = true)
    override fun buscarPorIdETelefone(id: Long, telefone: String): Reserva {
        val reserva = buscarPorId(id)
        if (reserva.cliente.telefone != telefone) {
            throw TelefoneInvalidoException()
        }
        return reserva
    }

    @Transactional
    override fun cancelarReserva(id: Long, telefone: String): Reserva {
        val reserva = buscarPorIdETelefone(id, telefone)
        ReservaStatusPolicy.validar(reserva.status, StatusReserva.CANCELADA)
        reserva.status = StatusReserva.CANCELADA
        val reservaSalva = reservaRepository.save(reserva)

        eventPublisher.publishEvent(ReservaAlteradaEvent(reservaSalva))

        return reservaSalva
    }

    @Transactional
    override fun atualizarStatus(id: Long, status: StatusReserva): Reserva {
        val reserva = buscarPorId(id)
        ReservaStatusPolicy.validar(reserva.status, status)
        reserva.status = status
        val reservaSalva = reservaRepository.save(reserva)

        eventPublisher.publishEvent(ReservaAlteradaEvent(reservaSalva))

        return reservaSalva
    }

    @Transactional(readOnly = true)
    override fun listarTodas(): List<Reserva> {
        return reservaRepository.findAllComDetalhes()
    }

    /**
     * A validação de disponibilidade roda antes, mas duas requisições simultâneas podem passar
     * por ela ao mesmo tempo. O flush aqui é proposital: traz a violação do constraint de exclusão
     * para dentro deste método, onde ela vira um 409 em vez de um 500 no commit.
     */
    private fun salvarComProtecaoContraSobreposicao(
        reserva: Reserva,
        mesaId: Long,
        dataHora: OffsetDateTime
    ): Reserva {
        return try {
            reservaRepository.saveAndFlush(reserva)
        } catch (ex: DataIntegrityViolationException) {
            if (ex.constraintName() == CONSTRAINT_SOBREPOSICAO) {
                throw ConflitoReservaException.paraMesa(mesaId, dataHora)
            }
            throw ex
        }
    }

    private fun DataIntegrityViolationException.constraintName(): String? {
        return generateSequence<Throwable>(this) { it.cause }
            .filterIsInstance<ConstraintViolationException>()
            .firstOrNull()
            ?.constraintName
    }

    private companion object {
        const val CONSTRAINT_SOBREPOSICAO = "ex_reserva_sem_sobreposicao"
    }
}
