package com.mesalive.repository

import com.mesalive.domain.Reserva
import com.mesalive.domain.StatusReserva
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface ReservaRepository : JpaRepository<Reserva, Long> {

    @Query("""
        SELECT r FROM Reserva r 
        WHERE r.mesa.id = :mesaId 
        AND r.status = :status 
        AND r.dataHora BETWEEN :inicio AND :fim
    """)
    fun findReservasConflitantes(
        @Param("mesaId") mesaId: Long,
        @Param("status") status: StatusReserva,
        @Param("inicio") inicio: OffsetDateTime,
        @Param("fim") fim: OffsetDateTime
    ): List<Reserva>
}
