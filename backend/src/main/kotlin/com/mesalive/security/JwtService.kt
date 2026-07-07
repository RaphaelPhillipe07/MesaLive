package com.mesalive.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secretString: String

    @Value("\${jwt.expiration:86400000}")
    private var jwtExpirationMs: Long = 86400000

    private val key by lazy { Keys.hmacShaKeyFor(secretString.toByteArray()) }

    fun gerarToken(email: String, role: String): String {
        val formatoRole = if (role.startsWith("ROLE_")) role else "ROLE_$role"

        return Jwts.builder()
            .subject(email)
            .claim("role", formatoRole)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(key)
            .compact()
    }

    private fun extrairTodosClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }

    fun extrairEmail(token: String): String? {
        return extrairTodosClaims(token)?.subject
    }

    fun extrairRole(token: String): String? {
        return extrairTodosClaims(token)?.get("role", String::class.java)
    }

    fun validarToken(token: String, email: String): Boolean {
        val emailExtraido = extrairEmail(token)
        return emailExtraido == email && !isTokenExpirado(token)
    }

    private fun isTokenExpirado(token: String): Boolean {
        val expiration = extrairTodosClaims(token)?.expiration ?: return true
        return expiration.before(Date())
    }
}