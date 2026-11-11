package com.mesalive.repository

import com.mesalive.domain.Reserva
import com.mesalive.domain.StatusReserva
import com.mesalive.dto.ReservaPainelResumoDTO
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface ReservaRepository : JpaRepository<Reserva, Long> {

    /**
     * Duas reservas se sobrepõem quando os intervalos [dataHora, dataHoraFim) se cruzam.
     * O mesmo critério do constraint `ex_reserva_sem_sobreposicao`.
     */
    @Query("""
        SELECT COUNT(r) FROM Reserva r
        WHERE r.mesa.id = :mesaId
          AND r.status IN :statuses
          AND r.dataHora < :fim
          AND r.dataHoraFim > :inicio
    """)
    fun countSobreposicoes(
        @Param("mesaId") mesaId: Long,
        @Param("statuses") statuses: Collection<StatusReserva>,
        @Param("inicio") inicio: OffsetDateTime,
        @Param("fim") fim: OffsetDateTime
    ): Long

    /**
     * Reservas relevantes para o mapa do salão, em uma única consulta:
     *  - CLIENTE_CHEGOU / NO_SHOW seguem no mapa até serem liberados (sem corte por data);
     *  - CONFIRMADA entra apenas dentro da janela de tolerância do horário.
     */
    @Query("""
        SELECT new com.mesalive.dto.ReservaPainelResumoDTO(
            r.mesa.id, r.id, r.cliente.nome, r.dataHora, r.status
        )
        FROM Reserva r
        WHERE r.status IN :statusesPermanentes
           OR (r.status = :statusConfirmada AND r.dataHora BETWEEN :inicio AND :fim)
    """)
    fun findResumosParaPainel(
        @Param("inicio") inicio: OffsetDateTime,
        @Param("fim") fim: OffsetDateTime,
        @Param("statusesPermanentes") statusesPermanentes: Collection<StatusReserva>,
        @Param("statusConfirmada") statusConfirmada: StatusReserva
    ): List<ReservaPainelResumoDTO>

    /** Listagem administrativa sem N+1: mesa e cliente vêm no mesmo SELECT. */
    @EntityGraph(attributePaths = ["mesa", "cliente"])
    @Query("SELECT r FROM Reserva r ORDER BY r.dataHora DESC")
    fun findAllComDetalhes(): List<Reserva>
}
