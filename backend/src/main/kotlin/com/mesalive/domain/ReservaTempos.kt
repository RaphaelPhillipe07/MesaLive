package com.mesalive.domain

/**
 * Regras de tempo do salão, em um único lugar.
 *
 * A duração da reserva é o que define quando duas reservas na mesma mesa se sobrepõem:
 * uma reserva ocupa o intervalo [dataHora, dataHora + DURACAO_PADRAO_MINUTOS).
 * As tolerâncias definem quando uma reserva aparece no mapa do salão.
 */
object ReservaTempos {
    /** Tempo padrão que uma mesa fica comprometida por uma reserva. */
    const val DURACAO_PADRAO_MINUTOS = 90L

    /** Quanto tempo antes do horário a reserva já aparece como "Reservada" no painel. */
    const val TOLERANCIA_ENTRADA_MINUTOS = 30L

    /** Por quanto tempo depois do horário a reserva continua relevante no painel. */
    const val TOLERANCIA_SAIDA_MINUTOS = 90L
}
