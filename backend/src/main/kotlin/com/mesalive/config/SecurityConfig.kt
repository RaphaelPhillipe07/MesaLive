package com.mesalive.config

import com.mesalive.security.JwtAuthenticationFilter
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
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
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
                    
                    // Handshake WebSocket e OpenAPI/Swagger
                    .requestMatchers("/ws/**").permitAll()
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
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
