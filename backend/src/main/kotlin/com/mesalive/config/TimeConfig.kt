package com.mesalive.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class TimeConfig {

    /** Relógio injetável: permite fixar o horário em testes do mapa do salão. */
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}
