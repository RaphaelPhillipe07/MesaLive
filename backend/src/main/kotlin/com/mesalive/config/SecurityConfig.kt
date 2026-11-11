package com.mesalive.config

import com.mesalive.security.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    @Value("\${app.cors.allowed-origins:http://localhost:4200}") private val allowedOrigins: String
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Endpoints Públicos de Reservas
                    .requestMatchers(HttpMethod.POST, "/api/reservas").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reservas/*").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/api/reservas/*").permitAll()
                    
                    // Endpoint de Login e Mesas Públicas
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/mesas/publicas").permitAll()
                    
                    // OpenAPI/Swagger
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    
                    // Endpoints Administrativos
                    .requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("GERENTE")
                    .requestMatchers(HttpMethod.GET, "/api/mesas").hasAnyRole("GERENTE", "GARCOM")
                    .requestMatchers(HttpMethod.PATCH, "/api/mesas/*/status").hasAnyRole("GERENTE", "GARCOM")
                    .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/status").hasAnyRole("GERENTE", "GARCOM")
                    .requestMatchers(HttpMethod.GET, "/api/reservas").hasAnyRole("GERENTE", "GARCOM")
                    
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        // Origens permitidas vem de app.cors.allowed-origins (CORS_ALLOWED_ORIGINS), nunca "*".
        configuration.allowedOriginPatterns = allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
