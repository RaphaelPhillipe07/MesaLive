package com.mesalive.support

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres

/**
 * Postgres real e efêmero para os testes de integração, sem Docker.
 * A instância é compartilhada entre as classes de teste para não pagar o start duas vezes.
 */
object EmbeddedPostgresSupport {

    private val instance: EmbeddedPostgres by lazy { EmbeddedPostgres.start() }

    val jdbcUrl: String by lazy { instance.getJdbcUrl("postgres", "postgres") }

    const val USERNAME = "postgres"
    const val PASSWORD = ""
}
