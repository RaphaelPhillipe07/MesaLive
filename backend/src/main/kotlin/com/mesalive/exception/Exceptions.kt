package com.mesalive.exception

import java.time.OffsetDateTime

open class NegocioException(message: String) : RuntimeException(message)

class MesaNaoEncontradaException(id: Long) : NegocioException("Mesa com ID $id não foi encontrada.")

class MesaInativaException(id: Long) : NegocioException("Mesa com ID $id não está ativa para reservas.")

class CapacidadeMesaInsuficienteException(mesaId: Long, capacidade: Int, solicitada: Int) : 
    NegocioException("Mesa com ID $mesaId tem capacidade máxima de $capacidade pessoas, mas foram solicitadas $solicitada vagas.")

class ReservaNaoEncontradaException(id: Long) : NegocioException("Reserva com ID $id não foi encontrada.")

class TelefoneInvalidoException : NegocioException("O telefone informado não confere com o da reserva.")

class ConflitoReservaException(mesaId: Long, dataHora: OffsetDateTime) : 
    NegocioException("A mesa $mesaId já possui uma reserva confirmada para o horário $dataHora ou próximo a ele.") {
    companion object {
        fun paraMesa(mesaId: Long, dataHora: OffsetDateTime) = ConflitoReservaException(mesaId, dataHora)
    }
}

class CredenciaisInvalidasException : NegocioException("Usuário ou senha incorretos.")
