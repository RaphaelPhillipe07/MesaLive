package com.mesalive.service.impl

import com.mesalive.domain.Reserva
import com.mesalive.domain.StatusReserva
import com.mesalive.event.ReservaAlteradaEvent
import com.mesalive.exception.*
import com.mesalive.repository.ReservaRepository
import com.mesalive.service.ClienteService
import com.mesalive.service.MesaService
import com.mesalive.service.ReservaService
import com.mesalive.service.validator.DisponibilidadeValidator
import org.springframework.context.ApplicationEventPublisher
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

        validators.forEach { it.validar(mesa, dataHora) }

        val cliente = clienteService.buscarOuCriarCliente(nomeCliente, telefoneCliente, emailCliente)

        val reserva = Reserva(
            mesa = mesa,
            cliente = cliente,
            dataHora = dataHora,
            quantidadePessoas = quantidadePessoas,
            status = StatusReserva.CONFIRMADA
        )

        val reservaSalva = reservaRepository.save(reserva)

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
        reserva.status = StatusReserva.CANCELADA
        val reservaSalva = reservaRepository.save(reserva)

        eventPublisher.publishEvent(ReservaAlteradaEvent(reservaSalva))

        return reservaSalva
    }

    @Transactional
    override fun atualizarStatus(id: Long, status: StatusReserva): Reserva {
        val reserva = buscarPorId(id)
        reserva.status = status
        val reservaSalva = reservaRepository.save(reserva)

        eventPublisher.publishEvent(ReservaAlteradaEvent(reservaSalva))

        return reservaSalva
    }

    @Transactional(readOnly = true)
    override fun listarTodas(): List<Reserva> {
        return reservaRepository.findAll()
    }
}
