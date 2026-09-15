# Guia Passo a Passo: Criando o MesaLive do Zero 🍽️

Este guia prático foi elaborado para permitir que você recrie o projeto **MesaLive** do absoluto zero, passo a passo, detalhando todas as etapas de configuração, os comandos executados, a estrutura de pastas e o código completo de cada arquivo.

---

## 🛠️ Pré-requisitos
Certifique-se de ter as seguintes ferramentas instaladas no seu sistema:
1. **Java Development Kit (JDK) 17** (LTS)
2. **Node.js** (v18 ou superior) e **npm**
3. **Docker** e **Docker Compose**
4. **IntelliJ IDEA** (Community ou Ultimate)

---

## 📁 1. Configuração do Diretório Raiz e Docker
Crie uma pasta chamada `MesaLive` e, dentro dela, o arquivo de orquestração do banco de dados local.

### Arquivo `MesaLive/docker-compose.yml`
```yaml
services:
  db:
    image: postgres:15-alpine
    container_name: mesalive-db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: mesalive
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
    driver: local
```

* **Comando para subir o banco local:**
  ```bash
  docker-compose up -d
  ```

---

## ☕ 2. Inicialização e Configuração do Backend (Spring Boot + Kotlin)

### Passo 2.1: Geração do Projeto com Spring Initializr
Execute o seguinte comando no PowerShell dentro da pasta raiz `MesaLive` para baixar o projeto base do Spring Initializr:

```powershell
Invoke-WebRequest -Uri "https://start.spring.io/starter.zip?type=gradle-project-kotlin&language=kotlin&baseDir=backend&groupId=com.mesalive&artifactId=backend&name=backend&description=MesaLive+Backend&packageName=com.mesalive&packaging=jar&javaVersion=17&dependencies=web,security,data-jpa,postgresql,flyway,websocket,validation" -OutFile "backend.zip"
```

Extraia e exclua o zip:
```powershell
Expand-Archive -Path "backend.zip" -DestinationPath "." -Force
Remove-Item -Path "backend.zip" -Force
```

---

### Passo 2.2: Dependências Customizadas
Abra o arquivo `backend/build.gradle.kts` e adicione dependências adicionais (Swagger UI, JWT e ferramentas de teste/MockK) dentro do bloco `dependencies`:

```kotlin
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	runtimeOnly("org.postgresql:postgresql")

	// Swagger Open API
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

	// Testing dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-websocket-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("io.mockk:mockk:1.13.11")
	testImplementation("org.testcontainers:junit-jupiter:1.19.8")
	testImplementation("org.testcontainers:postgresql:1.19.8")
}
```

---

### Passo 2.3: Configuração de Propriedades
Sob a pasta `backend/src/main/resources/`, substitua o conteúdo do arquivo `application.properties` por:

```properties
spring.application.name=mesalive-backend

# Conexão com o Banco PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/mesalive
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# Configurações do Hibernate / JPA
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Ativação do Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# Porta do Backend
server.port=8080

# Níveis de Logs
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
```

---

### Passo 2.4: Migrations do Banco de Dados (Flyway)
Crie o diretório `backend/src/main/resources/db/migration/` e adicione dois arquivos SQL:

#### 1. `db/migration/V1__create_initial_schema.sql`
```sql
CREATE TABLE mesa (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    capacidade INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(100)
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE reserva (
    id BIGSERIAL PRIMARY KEY,
    mesa_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    data_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    quantidade_pessoas INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_reserva_mesa FOREIGN KEY (mesa_id) REFERENCES mesa(id) ON DELETE CASCADE,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE
);
```

#### 2. `db/migration/V2__seed_initial_data.sql`
```sql
-- Cadastrar mesas físicas
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 1', 2, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 2', 2, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 3', 4, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 4', 4, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 5', 6, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 6', 8, true);

-- Cadastrar usuários de staff padrão
-- Senha de login: 'admin123'
INSERT INTO usuario (nome, email, senha_hash, role) VALUES ('Admin', 'admin@mesalive.com', '$2a$12$R.S2u/d53mG19x64vG4Qh.uWJ7y7zT66Bv2BvqG1oW917U/G0Uq2.', 'GERENTE');
INSERT INTO usuario (nome, email, senha_hash, role) VALUES ('Garçom João', 'joao@mesalive.com', '$2a$12$R.S2u/d53mG19x64vG4Qh.uWJ7y7zT66Bv2BvqG1oW917U/G0Uq2.', 'GARCOM');
```

---

## 🧱 3. Implementando o Código do Backend

### Passo 3.1: Entidades de Domínio (`com.mesalive.domain`)

#### `Mesa.kt`
```kotlin
package com.mesalive.domain

import jakarta.persistence.*

@Entity
@Table(name = "mesa")
class Mesa(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val numero: String,
    @Column(nullable = false)
    val capacidade: Int,
    @Column(nullable = false)
    var ativo: Boolean = true
)
```

#### `Cliente.kt`
```kotlin
package com.mesalive.domain

import jakarta.persistence.*

@Entity
@Table(name = "cliente")
class Cliente(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val nome: String,
    @Column(nullable = false)
    val telefone: String,
    @Column
    val email: String? = null
)
```

#### `Usuario.kt`
```kotlin
package com.mesalive.domain

import jakarta.persistence.*

enum class Role { GARCOM, GERENTE }

@Entity
@Table(name = "usuario")
class Usuario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val nome: String,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(name = "senha_hash", nullable = false)
    val senhaHash: String,
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    val role: Role
)
```

#### `Reserva.kt`
```kotlin
package com.mesalive.domain

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class StatusReserva { CONFIRMADA, CANCELADA, CLIENTE_CHEGOU, NO_SHOW }

@Entity
@Table(name = "reserva")
class Reserva(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "mesa_id", nullable = false)
    val mesa: Mesa,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false)
    val cliente: Cliente,
    @Column(name = "data_hora", nullable = false)
    val dataHora: OffsetDateTime,
    @Column(name = "quantidade_pessoas", nullable = false)
    val quantidadePessoas: Int,
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var status: StatusReserva = StatusReserva.CONFIRMADA
)
```

---

### Passo 3.2: Repositórios (`com.mesalive.repository`)

#### `MesaRepository.kt`
```kotlin
package com.mesalive.repository

import com.mesalive.domain.Mesa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MesaRepository : JpaRepository<Mesa, Long> {
    fun findByNumero(numero: String): Mesa?
}
```

#### `ClienteRepository.kt`
```kotlin
package com.mesalive.repository

import com.mesalive.domain.Cliente
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClienteRepository : JpaRepository<Cliente, Long> {
    fun findByTelefone(telefone: String): Cliente?
}
```

#### `UsuarioRepository.kt`
```kotlin
package com.mesalive.repository

import com.mesalive.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmail(email: String): Usuario?
}
```

#### `ReservaRepository.kt`
```kotlin
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
```

---

### Passo 3.3: Exceções Customizadas (`com.mesalive.exception`)

#### `Exceptions.kt`
```kotlin
package com.mesalive.exception

import java.time.OffsetDateTime

open class NegocioException(message: String) : RuntimeException(message)
class MesaNaoEncontradaException(id: Long) : NegocioException("Mesa com ID $id não foi encontrada.")
class MesaInativaException(id: Long) : NegocioException("Mesa com ID $id não está ativa.")
class CapacidadeMesaInsuficienteException(mesaId: Long, cap: Int, req: Int) : NegocioException("Mesa $mesaId tem capacidade máxima de $cap pessoas, mas foram solicitadas $req vagas.")
class ReservaNaoEncontradaException(id: Long) : NegocioException("Reserva com ID $id não foi encontrada.")
class TelefoneInvalidoException : NegocioException("O telefone informado não confere com o da reserva.")
class CredenciaisInvalidasException : NegocioException("E-mail ou senha incorretos.")

class ConflitoReservaException(mesaId: Long, dataHora: OffsetDateTime) : 
    NegocioException("A mesa $mesaId já possui uma reserva confirmada para o horário $dataHora.") {
    companion object {
        fun paraMesa(mesaId: Long, dataHora: OffsetDateTime) = ConflitoReservaException(mesaId, dataHora)
    }
}
```

#### `GlobalExceptionHandler.kt`
```kotlin
package com.mesalive.exception

import com.mesalive.dto.ErrorResponseDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MesaNaoEncontradaException::class, ReservaNaoEncontradaException::class)
    fun handleNotFound(ex: RuntimeException): ResponseEntity<ErrorResponseDTO> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponseDTO(HttpStatus.NOT_FOUND.value(), "Recurso Não Encontrado", ex.message ?: "")
        )

    @ExceptionHandler(ConflitoReservaException::class)
    fun handleConflict(ex: ConflitoReservaException): ResponseEntity<ErrorResponseDTO> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponseDTO(HttpStatus.CONFLICT.value(), "Conflito de Horário", ex.message ?: "")
        )

    @ExceptionHandler(NegocioException::class)
    fun handleBusiness(ex: NegocioException): ResponseEntity<ErrorResponseDTO> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponseDTO(HttpStatus.BAD_REQUEST.value(), "Erro de Negócio", ex.message ?: "")
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDTO> {
        val errors = ex.bindingResult.allErrors.associate { (it as FieldError).field to (it.defaultMessage ?: "Valor inválido") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponseDTO(HttpStatus.BAD_REQUEST.value(), "Erro de Validação", "Campos incorretos.", fieldErrors = errors)
        )
    }
}
```

---

### Passo 3.4: Serviços e Strategy de Validação (`com.mesalive.service`)

#### `DisponibilidadeValidator.kt` (na pasta `validator/`)
```kotlin
package com.mesalive.service.validator

import com.mesalive.domain.Mesa
import java.time.OffsetDateTime

interface DisponibilidadeValidator {
    fun validar(mesa: Mesa, dataHora: OffsetDateTime)
}
```

#### `ConflitoHorarioValidator.kt` (na pasta `validator/`)
```kotlin
package com.mesalive.service.validator

import com.mesalive.domain.Mesa
import com.mesalive.domain.StatusReserva
import com.mesalive.exception.ConflitoReservaException
import com.mesalive.repository.ReservaRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class ConflitoHorarioValidator(
    private val reservaRepository: ReservaRepository
) : DisponibilidadeValidator {

    override fun validar(mesa: Mesa, dataHora: OffsetDateTime) {
        val mesaId = mesa.id ?: return
        val inicio = dataHora.minusMinutes(89)
        val fim = dataHora.plusMinutes(89)

        val conflitos = reservaRepository.findReservasConflitantes(mesaId, StatusReserva.CONFIRMADA, inicio, fim)
        if (conflitos.isNotEmpty()) {
            throw ConflitoReservaException.paraMesa(mesaId, dataHora)
        }
    }
}
```

#### `MesaService.kt`
```kotlin
package com.mesalive.service

import com.mesalive.domain.Mesa

interface MesaService {
    fun listarTodas(): List<Mesa>
    fun listarAtivas(): List<Mesa>
    fun buscarPorId(id: Long): Mesa
    fun salvar(mesa: Mesa): Mesa
    fun atualizarStatus(id: Long, ativo: Boolean): Mesa
}
```

#### `MesaServiceImpl.kt` (na pasta `impl/`)
```kotlin
package com.mesalive.service.impl

import com.mesalive.domain.Mesa
import com.mesalive.exception.MesaNaoEncontradaException
import com.mesalive.repository.MesaRepository
import com.mesalive.service.MesaService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MesaServiceImpl(private val mesaRepository: MesaRepository) : MesaService {
    @Transactional(readOnly = true) override fun listarTodas(): List<Mesa> = mesaRepository.findAll()
    @Transactional(readOnly = true) override fun listarAtivas(): List<Mesa> = mesaRepository.findAll().filter { it.ativo }
    @Transactional(readOnly = true) override fun buscarPorId(id: Long): Mesa = mesaRepository.findById(id).orElseThrow { MesaNaoEncontradaException(id) }
    @Transactional override fun salvar(mesa: Mesa): Mesa = mesaRepository.save(mesa)
    @Transactional override fun atualizarStatus(id: Long, ativo: Boolean): Mesa {
        val mesa = buscarPorId(id)
        mesa.ativo = ativo
        return mesaRepository.save(mesa)
    }
}
```

#### `ClienteService.kt`
```kotlin
package com.mesalive.service

import com.mesalive.domain.Cliente

interface ClienteService {
    fun buscarPorId(id: Long): Cliente
    fun buscarOuCriarCliente(nome: String, telefone: String, email: String?): Cliente
}
```

#### `ClienteServiceImpl.kt` (na pasta `impl/`)
```kotlin
package com.mesalive.service.impl

import com.mesalive.domain.Cliente
import com.mesalive.exception.NegocioException
import com.mesalive.repository.ClienteRepository
import com.mesalive.service.ClienteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClienteServiceImpl(private val clienteRepository: ClienteRepository) : ClienteService {
    @Transactional(readOnly = true) override fun buscarPorId(id: Long): Cliente = clienteRepository.findById(id).orElseThrow { NegocioException("Cliente com ID $id não encontrado.") }
    @Transactional override fun buscarOuCriarCliente(nome: String, telefone: String, email: String?): Cliente {
        val existente = clienteRepository.findByTelefone(telefone)
        if (existente != null) {
            val atualizado = Cliente(existente.id, nome, telefone, email ?: existente.email)
            return clienteRepository.save(atualizado)
        }
        return clienteRepository.save(Cliente(nome = nome, telefone = telefone, email = email))
    }
}
```

#### `ReservaService.kt`
```kotlin
package com.mesalive.service

import com.mesalive.domain.Reserva
import com.mesalive.domain.StatusReserva
import java.time.OffsetDateTime

interface ReservaService {
    fun criarReserva(mesaId: Long, nomeCliente: String, telefoneCliente: String, emailCliente: String?, dataHora: OffsetDateTime, quantidadePessoas: Int): Reserva
    fun buscarPorId(id: Long): Reserva
    fun buscarPorIdETelefone(id: Long, telefone: String): Reserva
    fun cancelarReserva(id: Long, telefone: String): Reserva
    fun atualizarStatus(id: Long, status: StatusReserva): Reserva
    fun listarTodas(): List<Reserva>
}
```

#### `ReservaServiceImpl.kt` (na pasta `impl/`)
```kotlin
package com.mesalive.service.impl

import com.mesalive.domain.Reserva
import com.mesalive.domain.StatusReserva
import com.mesalive.event.ReservaAlteradaEvent
import com.mesalive.exception.*
import com.mesalive.repository.ReservaRepository
import com.mesalive.service.*
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
    override fun criarReserva(mesaId: Long, nomeCliente: String, telefoneCliente: String, emailCliente: String?, dataHora: OffsetDateTime, quantidadePessoas: Int): Reserva {
        val mesa = mesaService.buscarPorId(mesaId)
        if (!mesa.ativo) throw MesaInativaException(mesaId)
        if (quantidadePessoas > mesa.capacidade) throw CapacidadeMesaInsuficienteException(mesaId, mesa.capacidade, quantidadePessoas)

        validators.forEach { it.validar(mesa, dataHora) }

        val cliente = clienteService.buscarOuCriarCliente(nomeCliente, telefoneCliente, emailCliente)
        val reserva = Reserva(mesa = mesa, cliente = cliente, dataHora = dataHora, quantidadePessoas = quantidadePessoas, status = StatusReserva.CONFIRMADA)
        val salva = reservaRepository.save(reserva)

        eventPublisher.publishEvent(ReservaAlteradaEvent(salva))
        return salva
    }

    @Transactional(readOnly = true) override fun buscarPorId(id: Long): Reserva = reservaRepository.findById(id).orElseThrow { ReservaNaoEncontradaException(id) }

    @Transactional(readOnly = true)
    override fun buscarPorIdETelefone(id: Long, telefone: String): Reserva {
        val r = buscarPorId(id)
        if (r.cliente.telefone != telefone) throw TelefoneInvalidoException()
        return r
    }

    @Transactional
    override fun cancelarReserva(id: Long, telefone: String): Reserva {
        val r = buscarPorIdETelefone(id, telefone)
        r.status = StatusReserva.CANCELADA
        val salva = reservaRepository.save(r)
        eventPublisher.publishEvent(ReservaAlteradaEvent(salva))
        return salva
    }

    @Transactional
    override fun atualizarStatus(id: Long, status: StatusReserva): Reserva {
        val r = buscarPorId(id)
        r.status = status
        val salva = reservaRepository.save(r)
        eventPublisher.publishEvent(ReservaAlteradaEvent(salva))
        return salva
    }

    @Transactional(readOnly = true) override fun listarTodas(): List<Reserva> = reservaRepository.findAll()
}
```

---

### Passo 3.5: Eventos e WebSocket Config (`com.mesalive.event`, `com.mesalive.websocket`)

#### `ReservaAlteradaEvent.kt` (na pasta `event/`)
```kotlin
package com.mesalive.event

import com.mesalive.domain.Reserva
import org.springframework.context.ApplicationEvent

class ReservaAlteradaEvent(val reserva: Reserva) : ApplicationEvent(reserva)
```

#### `ReservaEventListener.kt` (na pasta `websocket/`)
```kotlin
package com.mesalive.websocket

import com.mesalive.event.ReservaAlteradaEvent
import com.mesalive.mapper.toDTO
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class ReservaEventListener(private val messagingTemplate: SimpMessagingTemplate) {
    @EventListener
    fun handleReservaAlterada(event: ReservaAlteradaEvent) {
        messagingTemplate.convertAndSend("/topic/salao", event.reserva.toDTO())
    }
}
```

#### `WebSocketConfig.kt` (na pasta `config/`)
```kotlin
package com.mesalive.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.*

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS()
    }
}
```

---

### Passo 3.6: Segurança e JWT Config (`com.mesalive.security`)

#### `JwtService.kt`
```kotlin
package com.mesalive.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService {
    private val secret = "minha-chave-secreta-muito-segura-e-longa-para-o-projeto-mesalive-123456"
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())
    private val expMs = 86400000

    fun gerarToken(email: String, role: String): String = Jwts.builder()
        .subject(email).claim("role", "ROLE_$role").issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + expMs)).signWith(key).compact()

    fun extrairEmail(token: String): String? = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject
    } catch (e: Exception) { null }

    fun extrairRole(token: String): String? = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload["role"] as? String
    } catch (e: Exception) { null }

    fun validarToken(token: String, email: String): Boolean = extrairEmail(token) == email && !isExpirado(token)

    private fun isExpirado(token: String): Boolean = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.expiration.before(Date())
    } catch (e: Exception) { true }
}
```

#### `JwtAuthenticationFilter.kt`
```kotlin
package com.mesalive.security

import com.mesalive.repository.UsuarioRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val usuarioRepository: UsuarioRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            val email = jwtService.extrairEmail(token)
            if (email != null && SecurityContextHolder.getContext().authentication == null) {
                val u = usuarioRepository.findByEmail(email)
                if (u != null && jwtService.validarToken(token, u.email)) {
                    val auth = UsernamePasswordAuthenticationToken(u, null, listOf(SimpleGrantedAuthority(jwtService.extrairRole(token) ?: "ROLE_GARCOM")))
                    auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        chain.doFilter(request, response)
    }
}
```

#### `SecurityConfig.kt` (na pasta `config/`)
```kotlin
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
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, "/api/reservas").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reservas/*").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/api/reservas/*").permitAll()
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/mesas/publicas").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
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
        val config = CorsConfiguration()
        config.allowedOriginPatterns = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("Authorization", "Content-Type")
        config.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
```

---

### Passo 3.7: Camada REST - DTOs, Mappers e Controllers (`com.mesalive.dto`, `com.mesalive.mapper`, `com.mesalive.controller`)

#### DTOs: `MesaDTOs.kt`
```kotlin
package com.mesalive.dto
data class MesaResponseDTO(val id: Long, val numero: String, val capacidade: Int, val ativo: Boolean)
data class MesaStatusRequestDTO(val ativo: Boolean)
```

#### DTOs: `ClienteDTOs.kt`
```kotlin
package com.mesalive.dto
data class ClienteResponseDTO(val id: Long, val nome: String, val telefone: String, val email: String?)
```

#### DTOs: `ReservaDTOs.kt`
```kotlin
package com.mesalive.dto

import com.mesalive.domain.StatusReserva
import jakarta.validation.constraints.*
import java.time.OffsetDateTime

data class ReservaRequestDTO(
    @field:NotNull(message = "Mesa obrigatória.") val mesaId: Long?,
    @field:NotBlank(message = "Nome obrigatório.") val nomeCliente: String?,
    @field:NotBlank(message = "Telefone obrigatório.") val telefoneCliente: String?,
    val emailCliente: String?,
    @field:NotNull(message = "Data obrigatória.") @field:Future(message = "A data deve estar no futuro.") val dataHora: OffsetDateTime?,
    @field:NotNull(message = "Quantidade obrigatória.") @field:Min(value = 1, message = "Mínimo 1 pessoa.") val quantidadePessoas: Int?
)

data class ReservaResponseDTO(val id: Long, val mesa: MesaResponseDTO, val cliente: ClienteResponseDTO, val dataHora: OffsetDateTime, val quantidadePessoas: Int, val status: StatusReserva)
data class ReservaStatusRequestDTO(@field:NotNull val status: StatusReserva?)
```

#### DTOs: `MesaPainelResponseDTO.kt`
```kotlin
package com.mesalive.dto
data class MesaPainelResponseDTO(val id: Long, val numero: String, val capacidade: Int, val ativo: Boolean, val statusAtual: String, val reservaAtivaId: Long? = null, val nomeCliente: String? = null, val dataHoraReserva: String? = null)
```

#### DTOs: `ErrorResponseDTO.kt`
```kotlin
package com.mesalive.dto
import java.time.OffsetDateTime
data class ErrorResponseDTO(val status: Int, val error: String, val message: String, val timestamp: OffsetDateTime = OffsetDateTime.now(), val fieldErrors: Map<String, String>? = null)
```

#### DTOs: `AuthDTOs.kt`
```kotlin
package com.mesalive.dto
import jakarta.validation.constraints.*
data class LoginRequestDTO(@field:NotBlank @field:Email val email: String?, @field:NotBlank val senha: String?)
data class LoginResponseDTO(val token: String, val nome: String, val email: String, val role: String)
```

#### `Mappers.kt` (na pasta `mapper/`)
```kotlin
package com.mesalive.mapper

import com.mesalive.domain.*
import com.mesalive.dto.*

fun Mesa.toDTO() = MesaResponseDTO(id ?: 0L, numero, capacidade, ativo)
fun Cliente.toDTO() = ClienteResponseDTO(id ?: 0L, nome, telefone, email)
fun Reserva.toDTO() = ReservaResponseDTO(id ?: 0L, mesa.toDTO(), cliente.toDTO(), dataHora, quantidadePessoas, status)
```

#### `AuthController.kt` (na pasta `controller/`)
```kotlin
package com.mesalive.controller

import com.mesalive.dto.LoginRequestDTO
import com.mesalive.dto.LoginResponseDTO
import com.mesalive.exception.CredenciaisInvalidasException
import com.mesalive.repository.UsuarioRepository
import com.mesalive.security.JwtService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val usuarioRepository: UsuarioRepository,
    private val encoder: PasswordEncoder,
    private val jwt: JwtService
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequestDTO): ResponseEntity<LoginResponseDTO> {
        val u = usuarioRepository.findByEmail(req.email!!) ?: throw CredenciaisInvalidasException()
        if (!encoder.matches(req.senha!!, u.senhaHash)) throw CredenciaisInvalidasException()
        return ResponseEntity.ok(LoginResponseDTO(jwt.gerarToken(u.email, u.role.name), u.nome, u.email, u.role.name))
    }
}
```

#### `MesaController.kt` (na pasta `controller/`)
```kotlin
package com.mesalive.controller

import com.mesalive.dto.*
import com.mesalive.domain.StatusReserva
import com.mesalive.service.MesaService
import com.mesalive.service.ReservaService
import com.mesalive.mapper.toDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/mesas")
class MesaController(
    private val mesaService: MesaService,
    private val reservaService: ReservaService
) {
    @GetMapping("/publicas")
    fun listarPublicas(): ResponseEntity<List<MesaResponseDTO>> =
        ResponseEntity.ok(mesaService.listarAtivas().map { it.toDTO() })

    @GetMapping
    fun listarMesasComStatus(): ResponseEntity<List<MesaPainelResponseDTO>> {
        val mesas = mesaService.listarTodas()
        val reservas = reservaService.listarTodas()
        val agora = OffsetDateTime.now()

        val response = mesas.map { mesa ->
            val resAtiva = reservas.filter { r -> r.mesa.id == mesa.id }
                .firstOrNull { r ->
                    when (r.status) {
                        StatusReserva.CLIENTE_CHEGOU -> true
                        StatusReserva.NO_SHOW -> r.dataHora.toLocalDate() == agora.toLocalDate()
                        StatusReserva.CONFIRMADA -> agora.isAfter(r.dataHora.minusMinutes(30)) && agora.isBefore(r.dataHora.plusMinutes(90))
                        StatusReserva.CANCELADA -> false
                    }
                }

            val status = when {
                !mesa.ativo -> "INATIVA"
                resAtiva == null -> "LIVRE"
                resAtiva.status == StatusReserva.CLIENTE_CHEGOU -> "OCUPADA"
                resAtiva.status == StatusReserva.NO_SHOW -> "NO_SHOW"
                resAtiva.status == StatusReserva.CONFIRMADA -> "RESERVADA"
                else -> "LIVRE"
            }

            MesaPainelResponseDTO(mesa.id ?: 0, mesa.numero, mesa.capacidade, mesa.ativo, status, resAtiva?.id, resAtiva?.cliente?.nome, resAtiva?.dataHora?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        }
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/status")
    fun atualizarStatus(@PathVariable id: Long, @RequestBody req: MesaStatusRequestDTO): ResponseEntity<MesaPainelResponseDTO> {
        val mesa = mesaService.atualizarStatus(id, req.ativo)
        return ResponseEntity.ok(MesaPainelResponseDTO(mesa.id ?: 0, mesa.numero, mesa.capacidade, mesa.ativo, if (mesa.ativo) "LIVRE" else "INATIVA"))
    }
}
```

#### `ReservaController.kt` (na pasta `controller/`)
```kotlin
package com.mesalive.controller

import com.mesalive.dto.*
import com.mesalive.mapper.toDTO
import com.mesalive.service.ReservaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservas")
class ReservaController(private val reservaService: ReservaService) {

    @PostMapping
    fun criar(@Valid @RequestBody req: ReservaRequestDTO): ResponseEntity<ReservaResponseDTO> {
        val r = reservaService.criarReserva(req.mesaId!!, req.nomeCliente!!, req.telefoneCliente!!, req.emailCliente, req.dataHora!!, req.quantidadePessoas!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(r.toDTO())
    }

    @GetMapping("/{id}")
    fun consultar(@PathVariable id: Long, @RequestParam telefone: String): ResponseEntity<ReservaResponseDTO> =
        ResponseEntity.ok(reservaService.buscarPorIdETelefone(id, telefone).toDTO())

    @DeleteMapping("/{id}")
    fun cancelar(@PathVariable id: Long, @RequestParam telefone: String): ResponseEntity<ReservaResponseDTO> =
        ResponseEntity.ok(reservaService.cancelarReserva(id, telefone).toDTO())

    @PatchMapping("/{id}/status")
    fun atualizarStatus(@PathVariable id: Long, @Valid @RequestBody req: ReservaStatusRequestDTO): ResponseEntity<ReservaResponseDTO> =
        ResponseEntity.ok(reservaService.atualizarStatus(id, req.status!!).toDTO())

    @GetMapping
    fun listarTodas(): ResponseEntity<List<ReservaResponseDTO>> =
        ResponseEntity.ok(reservaService.listarTodas().map { it.toDTO() })
}
```

---

### Passo 3.8: Classe Principal e Testes

#### `BackendApplication.kt` (em `com.mesalive`)
```kotlin
package com.mesalive
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackendApplication

fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}
```

#### Teste de Serviços: `backend/src/test/kotlin/com/mesalive/service/ReservaServiceTest.kt`
```kotlin
package com.mesalive.service

import com.mesalive.domain.*
import com.mesalive.exception.*
import com.mesalive.event.ReservaAlteradaEvent
import com.mesalive.repository.ReservaRepository
import com.mesalive.service.impl.ReservaServiceImpl
import com.mesalive.service.validator.DisponibilidadeValidator
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ReservaServiceTest {
    private val repo = mockk<ReservaRepository>()
    private val mService = mockk<MesaService>()
    private val cService = mockk<ClienteService>()
    private val validador = mockk<DisponibilidadeValidator>()
    private val publisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val service = ReservaServiceImpl(repo, mService, cService, listOf(validador), publisher)

    @Test
    fun `deve criar reserva com sucesso`() {
        val mesa = Mesa(1L, "Mesa 1", 4, true)
        val cliente = Cliente(1L, "João", "11999999999")
        val data = OffsetDateTime.now().plusDays(1)
        val reserva = Reserva(1L, mesa, cliente, data, 2)

        every { mService.buscarPorId(1L) } returns mesa
        every { validador.validar(mesa, data) } just Runs
        every { cService.buscarOuCriarCliente("João", "11999999999", any()) } returns cliente
        every { repo.save(any()) } returns reserva

        val res = service.criarReserva(1L, "João", "11999999999", null, data, 2)
        assertNotNull(res)
        assertEquals("Mesa 1", res.mesa.numero)
        verify { publisher.publishEvent(any<ReservaAlteradaEvent>()) }
    }

    @Test
    fun `deve lancar erro mesa inativa`() {
        val mesa = Mesa(1L, "Mesa 1", 4, false)
        every { mService.buscarPorId(1L) } returns mesa
        assertThrows<MesaInativaException> {
            service.criarReserva(1L, "João", "11999999999", null, OffsetDateTime.now().plusDays(1), 2)
        }
    }
}
```

---

## 🎨 4. Inicialização e Configuração do Frontend (Angular 18)

### Passo 4.1: Geração da Workspace e Instalação
No terminal na raiz do projeto `MesaLive`:

```bash
npx -y @angular/cli@18 new frontend --directory=frontend --routing --style=css --ssr=false --package-manager=npm --interactive=false
```

Navegue até a pasta frontend e instale as dependências para conexões websocket:
```bash
cd frontend
npm install @stomp/stompjs sockjs-client
npm install -D @types/sockjs-client
```

---

### Passo 4.2: Configurando Arquivos de Configuração

#### `frontend/src/index.html`
Substitua o cabeçalho `<head>` para importar as fontes modernas:
```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>MesaLive 🍽️</title>
  <base href="/">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="favicon.ico">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
  <app-root></app-root>
</body>
</html>
```

#### `frontend/src/app/app.config.ts`
Adicione o `provideHttpClient` para chamadas de API REST:
```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient()
  ]
};
```

#### `frontend/src/app/app.routes.ts`
Configure o roteador com os componentes:
```typescript
import { Routes } from '@angular/router';
import { ReservaClienteComponent } from './components/reserva-cliente/reserva-cliente.component';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';

export const routes: Routes = [
  { path: '', component: ReservaClienteComponent },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: '**', redirectTo: '' }
];
```

#### `frontend/src/app/app.component.html`
Substitua todo o HTML padrão por:
```html
<router-outlet></router-outlet>
```

---

### Passo 4.3: Serviços do Frontend (`frontend/src/app/services/`)

#### `auth.service.ts`
```typescript
import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tKey = 'mesalive_token';
  private uKey = 'mesalive_user';
  currentUser = signal<any>(null);

  constructor() { this.loadUser(); }

  login(token: string, user: any) {
    localStorage.setItem(this.tKey, token);
    localStorage.setItem(this.uKey, JSON.stringify(user));
    this.currentUser.set(user);
  }

  logout() {
    localStorage.removeItem(this.tKey);
    localStorage.removeItem(this.uKey);
    this.currentUser.set(null);
  }

  getToken(): string | null { return localStorage.getItem(this.tKey); }
  isAuthenticated(): boolean { return !!this.getToken(); }

  private loadUser() {
    const userStr = localStorage.getItem(this.uKey);
    if (userStr) {
      try { this.currentUser.set(JSON.parse(userStr)); } 
      catch (e) { this.logout(); }
    }
  }
}
```

#### `websocket.service.ts`
```typescript
import { Injectable, signal } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({ providedIn: 'root' })
export class WebsocketService {
  private stompClient: Client | null = null;
  private messageSubject = new Subject<any>();
  connected = signal<boolean>(false);

  connect() {
    if (this.stompClient && this.stompClient.connected) return;

    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
    });

    this.stompClient.onConnect = () => {
      this.connected.set(true);
      this.stompClient?.subscribe('/topic/salao', (message) => {
        if (message.body) {
          try { this.messageSubject.next(JSON.parse(message.body)); } 
          catch (e) { console.error(e); }
        }
      });
    };

    this.stompClient.onDisconnect = () => this.connected.set(false);
    this.stompClient.activate();
  }

  disconnect() {
    if (this.stompClient) this.stompClient.deactivate();
    this.connected.set(false);
  }

  onMessage(): Observable<any> { return this.messageSubject.asObservable(); }
}
```

#### `api.service.ts`
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    let headers = new HttpHeaders().set('Content-Type', 'application/json');
    if (token) headers = headers.set('Authorization', `Bearer ${token}`);
    return headers;
  }

  login(credentials: any): Observable<any> { return this.http.post(`${this.baseUrl}/auth/login`, credentials); }
  getMesas(): Observable<any[]> { return this.http.get<any[]>(`${this.baseUrl}/mesas`, { headers: this.getHeaders() }); }
  getMesasPublicas(): Observable<any[]> { return this.http.get<any[]>(`${this.baseUrl}/mesas/publicas`); }
  atualizarMesaStatus(id: number, ativo: boolean): Observable<any> { return this.http.patch(`${this.baseUrl}/mesas/${id}/status`, { ativo }, { headers: this.getHeaders() }); }
  criarReserva(reserva: any): Observable<any> { return this.http.post(`${this.baseUrl}/reservas`, reserva); }
  consultarReserva(id: number, tel: string): Observable<any> { return this.http.get(`${this.baseUrl}/reservas/${id}`, { params: new HttpParams().set('telefone', tel) }); }
  cancelarReserva(id: number, tel: string): Observable<any> { return this.http.delete(`${this.baseUrl}/reservas/${id}`, { params: new HttpParams().set('telefone', tel) }); }
  atualizarReservaStatus(id: number, status: string): Observable<any> { return this.http.patch(`${this.baseUrl}/reservas/${id}/status`, { status }, { headers: this.getHeaders() }); }
}
```

---

### Passo 4.4: Componente de Login (`frontend/src/app/components/login/`)

#### `login.component.ts`
```typescript
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  credentials = { email: '', senha: '' };
  errorMessage = '';
  isLoading = false;

  constructor(private api: ApiService, private auth: AuthService, private router: Router) {}

  onSubmit() {
    if (!this.credentials.email || !this.credentials.senha) {
      this.errorMessage = 'Preencha todos os campos.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';

    this.api.login(this.credentials).subscribe({
      next: (res) => {
        this.auth.login(res.token, { nome: res.nome, email: res.email, role: res.role });
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Credenciais incorretas ou erro de rede.';
      }
    });
  }
}
```

#### `login.component.html`
```html
<div class="login-container">
  <div class="glass-card">
    <div class="header">
      <div class="logo">🍽️</div>
      <h1>MesaLive Staff</h1>
      <p>Gerenciamento e reservas em tempo real</p>
    </div>
    <form (ngSubmit)="onSubmit()">
      <div class="form-group">
        <label for="email">E-mail</label>
        <input type="email" id="email" name="email" [(ngModel)]="credentials.email" required placeholder="exemplo@mesalive.com" />
      </div>
      <div class="form-group">
        <label for="password">Senha</label>
        <input type="password" id="password" name="password" [(ngModel)]="credentials.senha" required placeholder="••••••••" />
      </div>
      <div class="error-box" *ngIf="errorMessage">⚠️ {{ errorMessage }}</div>
      <button type="submit" [disabled]="isLoading" class="btn-login">
        <span *ngIf="!isLoading">Entrar no Painel</span>
        <span *ngIf="isLoading" class="spinner"></span>
      </button>
    </form>
  </div>
</div>
```

#### `login.component.css`
```css
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: radial-gradient(circle at 10% 20%, rgb(4, 15, 34) 0%, rgb(18, 5, 29) 90.2%);
  font-family: 'Outfit', 'Inter', sans-serif;
  color: #f1f5f9;
  padding: 20px;
}
.glass-card {
  width: 100%; max-width: 420px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px; padding: 40px;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
}
.header { text-align: center; margin-bottom: 35px; }
.logo { font-size: 3rem; margin-bottom: 15px; }
h1 {
  font-size: 1.85rem; font-weight: 700; margin: 0 0 8px 0;
  background: linear-gradient(135deg, #a78bfa 0%, #ec4899 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
p { font-size: 0.9rem; color: #94a3b8; margin: 0; }
form { display: flex; flex-direction: column; gap: 20px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
label { font-size: 0.85rem; font-weight: 600; color: #c084fc; text-transform: uppercase; }
input {
  background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px; padding: 14px; color: #fff; outline: none; transition: all 0.3s;
}
input:focus { border-color: #a78bfa; box-shadow: 0 0 12px rgba(167, 139, 250, 0.25); }
.error-box {
  background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 12px; padding: 12px; color: #fca5a5; font-size: 0.85rem;
}
.btn-login {
  background: linear-gradient(135deg, #8b5cf6 0%, #d946ef 100%);
  border: none; border-radius: 12px; padding: 16px; color: #fff; font-weight: 600;
  cursor: pointer; box-shadow: 0 4px 15px rgba(139, 92, 246, 0.4);
}
.btn-login:hover { transform: translateY(-2px); }
.spinner {
  width: 20px; height: 20px; border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%; border-top-color: #fff; animation: spin 1s infinite linear;
}
@keyframes spin { to { transform: rotate(360deg); } }
```

---

### Passo 4.5: Componente do Cliente (`frontend/src/app/components/reserva-cliente/`)

#### `reserva-cliente.component.ts`
```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-reserva-cliente',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reserva-cliente.component.html',
  styleUrls: ['./reserva-cliente.component.css']
})
export class ReservaClienteComponent implements OnInit {
  activeTab: 'nova' | 'minha' = 'nova';
  booking = { nomeCliente: '', telefoneCliente: '', emailCliente: '', mesaId: null as number | null, dataHora: '', quantidadePessoas: 2 };
  lookup = { id: null as number | null, telefone: '' };
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  createdBooking: any = null;
  searchedBooking: any = null;
  mesasDisponiveis: any[] = [];

  constructor(private api: ApiService, public router: Router) {}

  ngOnInit() { this.carregarMesas(); }

  carregarMesas() {
    this.api.getMesasPublicas().subscribe({
      next: (res) => this.mesasDisponiveis = res,
      error: () => console.warn('Sem conexão com a API de mesas.')
    });
  }

  setTab(tab: 'nova' | 'minha') {
    this.activeTab = tab;
    this.errorMessage = '';
    this.successMessage = '';
    this.createdBooking = null;
    this.searchedBooking = null;
  }

  onSubmitReserva() {
    if (!this.booking.nomeCliente || !this.booking.telefoneCliente || !this.booking.dataHora || !this.booking.mesaId) {
      this.errorMessage = 'Preencha todos os campos obrigatórios.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const offsetDate = new Date(this.booking.dataHora).toISOString();
    const payload = { ...this.booking, dataHora: offsetDate };

    this.api.criarReserva(payload).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.createdBooking = res;
        this.successMessage = 'Reserva confirmada!';
        this.booking = { nomeCliente: '', telefoneCliente: '', emailCliente: '', mesaId: null, dataHora: '', quantidadePessoas: 2 };
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.status === 409 ? 'Mesa já ocupada nesse horário.' : 'Erro ao realizar agendamento.';
      }
    });
  }

  onConsultar() {
    if (!this.lookup.id || !this.lookup.telefone) {
      this.errorMessage = 'ID e Telefone obrigatórios.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    this.api.consultarReserva(this.lookup.id, this.lookup.telefone).subscribe({
      next: (res) => { this.isLoading = false; this.searchedBooking = res; },
      error: () => { this.isLoading = false; this.errorMessage = 'Reserva não encontrada.'; }
    });
  }

  onCancelar(id: number, tel: string) {
    if (!confirm('Deseja cancelar?')) return;
    this.isLoading = true;
    this.api.cancelarReserva(id, tel).subscribe({
      next: (res) => { this.isLoading = false; alert('Cancelada!'); this.searchedBooking = res; },
      error: () => { this.isLoading = false; this.errorMessage = 'Erro ao cancelar.'; }
    });
  }

  formatDate(str: string): string { return new Date(str).toLocaleString('pt-BR'); }
}
```

#### `reserva-cliente.component.html`
```html
<div class="booking-container">
  <header class="app-header">
    <div class="brand"><span class="logo">🍽️</span><h1>MesaLive</h1></div>
    <button class="btn-staff" (click)="router.navigate(['/login'])">Área do Staff 🔑</button>
  </header>

  <div class="glass-box">
    <div class="tabs">
      <button class="tab-btn" [class.active]="activeTab === 'nova'" (click)="setTab('nova')">Nova Reserva</button>
      <button class="tab-btn" [class.active]="activeTab === 'minha'" (click)="setTab('minha')">Minhas Reservas</button>
    </div>

    <!-- TAB: NOVA -->
    <div class="tab-content" *ngIf="activeTab === 'nova'">
      <form (ngSubmit)="onSubmitReserva()" *ngIf="!createdBooking">
        <div class="form-row">
          <div class="form-group">
            <label>Nome Completo</label>
            <input type="text" name="nome" [(ngModel)]="booking.nomeCliente" required placeholder="Seu nome" />
          </div>
          <div class="form-group">
            <label>Telefone / WhatsApp</label>
            <input type="tel" name="tel" [(ngModel)]="booking.telefoneCliente" required placeholder="(00) 00000-0000" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>E-mail (Opcional)</label>
            <input type="email" name="email" [(ngModel)]="booking.emailCliente" placeholder="seuemail@exemplo.com" />
          </div>
          <div class="form-group">
            <label>Pessoas</label>
            <input type="number" name="pessoas" [(ngModel)]="booking.quantidadePessoas" required min="1" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>Data & Hora</label>
            <input type="datetime-local" name="data" [(ngModel)]="booking.dataHora" required />
          </div>
          <div class="form-group">
            <label>Escolha a Mesa</label>
            <select name="mesa" [(ngModel)]="booking.mesaId" required>
              <option [ngValue]="null" disabled>Selecione uma mesa</option>
              <option *ngFor="let mesa of mesasDisponiveis" [value]="mesa.id">
                {{ mesa.numero }} (Capacidade: {{ mesa.capacidade }})
              </option>
            </select>
          </div>
        </div>
        <div class="error-box" *ngIf="errorMessage">⚠️ {{ errorMessage }}</div>
        <button type="submit" class="btn-submit" [disabled]="isLoading">
          <span *ngIf="!isLoading">Confirmar Reserva 🍽️</span>
          <span *ngIf="isLoading" class="spinner"></span>
        </button>
      </form>

      <div class="success-card" *ngIf="createdBooking">
        <div class="success-icon">🎉</div>
        <h3>Reserva Confirmada!</h3>
        <p class="code">Código da sua reserva: <strong>#{{ createdBooking.id }}</strong></p>
        <div class="details">
          <div><strong>Mesa:</strong> {{ createdBooking.mesa.numero }}</div>
          <div><strong>Data:</strong> {{ formatDate(createdBooking.dataHora) }}</div>
          <div><strong>Pessoas:</strong> {{ createdBooking.quantidadePessoas }}</div>
        </div>
        <button class="btn-reset" (click)="createdBooking = null">Nova Reserva</button>
      </div>
    </div>

    <!-- TAB: MINHA -->
    <div class="tab-content" *ngIf="activeTab === 'minha'">
      <div class="search-box">
        <div class="form-group">
          <label>Código da Reserva (#)</label>
          <input type="number" [(ngModel)]="lookup.id" placeholder="Ex: 5" />
        </div>
        <div class="form-group">
          <label>Telefone</label>
          <input type="text" [(ngModel)]="lookup.telefone" placeholder="(00) 00000-0000" />
        </div>
        <button class="btn-search" (click)="onConsultar()">Buscar 🔍</button>
      </div>
      <div class="error-box" *ngIf="errorMessage">⚠️ {{ errorMessage }}</div>

      <div class="booking-card" *ngIf="searchedBooking">
        <div class="booking-header">
          <h3>Reserva #{{ searchedBooking.id }}</h3>
          <span class="badge" [ngClass]="searchedBooking.status.toLowerCase()">{{ searchedBooking.status }}</span>
        </div>
        <div class="details">
          <div><strong>Cliente:</strong> {{ searchedBooking.cliente.nome }}</div>
          <div><strong>Mesa:</strong> {{ searchedBooking.mesa.numero }}</div>
          <div><strong>Data:</strong> {{ formatDate(searchedBooking.dataHora) }}</div>
        </div>
        <div class="actions" *ngIf="searchedBooking.status === 'CONFIRMADA'">
          <button class="btn-cancel" (click)="onCancelar(searchedBooking.id, searchedBooking.cliente.telefone)">Cancelar Reserva</button>
        </div>
      </div>
    </div>
  </div>
</div>
```

#### `reserva-cliente.component.css`
```css
.booking-container {
  min-height: 100vh; background: radial-gradient(circle at 10% 20%, rgb(4, 15, 34) 0%, rgb(18, 5, 29) 90.2%);
  font-family: 'Outfit', 'Inter', sans-serif; color: #f1f5f9;
  padding: 30px 20px; display: flex; flex-direction: column; align-items: center;
}
.app-header { width: 100%; max-width: 800px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.brand { display: flex; align-items: center; gap: 12px; }
.brand h1 { font-size: 1.8rem; font-weight: 700; margin: 0; background: linear-gradient(135deg, #a78bfa 0%, #ec4899 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.logo { font-size: 2.2rem; }
.btn-staff { background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); color: #c084fc; padding: 10px 18px; border-radius: 12px; font-weight: 600; cursor: pointer; }
.btn-staff:hover { background: rgba(192, 132, 252, 0.1); border-color: #c084fc; }
.glass-box { width: 100%; max-width: 800px; background: rgba(255, 255, 255, 0.02); backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 0.06); border-radius: 24px; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.4); overflow: hidden; }
.tabs { display: flex; background: rgba(0, 0, 0, 0.2); }
.tab-btn { flex: 1; background: transparent; border: none; color: #94a3b8; padding: 20px; font-size: 1.05rem; font-weight: 600; cursor: pointer; border-bottom: 2px solid transparent; }
.tab-btn.active { color: #c084fc; border-bottom-color: #c084fc; }
.tab-content { padding: 40px; }
form { display: flex; flex-direction: column; gap: 24px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
label { font-size: 0.85rem; font-weight: 600; color: #c084fc; text-transform: uppercase; }
input, select { background: rgba(255, 255, 255, 0.04); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 12px; padding: 14px; color: #fff; outline: none; }
input:focus, select:focus { border-color: #a78bfa; }
.btn-submit { background: linear-gradient(135deg, #8b5cf6 0%, #d946ef 100%); border: none; border-radius: 12px; padding: 16px; color: #fff; font-size: 1.05rem; font-weight: 600; cursor: pointer; }
.success-card { text-align: center; display: flex; flex-direction: column; align-items: center; }
.success-icon { font-size: 4rem; margin-bottom: 15px; }
.success-card h3 { color: #4ade80; }
.success-card .details { background: rgba(0, 0, 0, 0.2); border-radius: 16px; padding: 20px; width: 100%; max-width: 450px; margin: 20px 0; text-align: left; display: flex; flex-direction: column; gap: 12px; }
.btn-reset { background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); color: #fff; border-radius: 12px; padding: 14px 24px; cursor: pointer; }
.search-box { display: flex; gap: 20px; align-items: flex-end; margin-bottom: 30px; }
.btn-search { background: #a78bfa; border: none; border-radius: 12px; padding: 14px 24px; color: #110d1a; font-weight: 700; cursor: pointer; height: 48px; }
.booking-card { background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 16px; padding: 24px; }
.booking-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255, 255, 255, 0.05); padding-bottom: 16px; }
.badge { font-size: 0.75rem; font-weight: 700; padding: 6px 12px; border-radius: 20px; text-transform: uppercase; }
.badge.confirmada { background: rgba(74, 222, 128, 0.15); color: #4ade80; border: 1px solid rgba(74, 222, 128, 0.3); }
.badge.cancelada { background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); }
.actions { display: flex; justify-content: flex-end; margin-top: 24px; }
.btn-cancel { background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); color: #fca5a5; border-radius: 12px; padding: 12px 20px; cursor: pointer; }
.error-box { background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: 12px; padding: 14px; color: #fca5a5; }
.spinner { width: 20px; height: 20px; border: 3px solid rgba(255, 255, 255, 0.3); border-radius: 50%; border-top-color: #fff; animation: spin 1s infinite linear; }
@keyframes spin { to { transform: rotate(360deg); } }
```

---

### Passo 4.6: Componente do Staff / Salão (`frontend/src/app/components/dashboard/`)

#### `dashboard.component.ts`
```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { WebsocketService } from '../../services/websocket.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  mesas: any[] = [];
  isLoading = false;
  usuario: any = null;
  private wsSubscription: Subscription | null = null;

  get countLivre() { return this.mesas.filter(m => m.statusAtual === 'LIVRE').length; }
  get countReservada() { return this.mesas.filter(m => m.statusAtual === 'RESERVADA').length; }
  get countOcupada() { return this.mesas.filter(m => m.statusAtual === 'OCUPADA').length; }
  get countNoShow() { return this.mesas.filter(m => m.statusAtual === 'NO_SHOW').length; }

  constructor(private api: ApiService, public auth: AuthService, private ws: WebsocketService, private router: Router) {}

  ngOnInit() {
    if (!this.auth.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    this.usuario = this.auth.currentUser();
    this.carregarDados();

    this.ws.connect();
    this.wsSubscription = this.ws.onMessage().subscribe(() => this.carregarDados());
  }

  ngOnDestroy() {
    if (this.wsSubscription) this.wsSubscription.unsubscribe();
    this.ws.disconnect();
  }

  carregarDados() {
    this.isLoading = true;
    this.api.getMesas().subscribe({
      next: (res) => { this.mesas = res; this.isLoading = false; },
      error: () => {
        this.isLoading = false;
        this.auth.logout();
        this.router.navigate(['/login']);
      }
    });
  }

  toggleAtivo(mesa: any) {
    const novoStatus = !mesa.ativo;
    this.api.atualizarMesaStatus(mesa.id, novoStatus).subscribe({
      next: () => {
        mesa.ativo = novoStatus;
        mesa.statusAtual = novoStatus ? 'LIVRE' : 'INATIVA';
      }
    });
  }

  marcarEntrada(reservaId: number) {
    this.api.atualizarReservaStatus(reservaId, 'CLIENTE_CHEGOU').subscribe(() => this.carregarDados());
  }

  marcarNoShow(reservaId: number) {
    this.api.atualizarReservaStatus(reservaId, 'NO_SHOW').subscribe(() => this.carregarDados());
  }

  liberarMesa(reservaId: number) {
    this.api.atualizarReservaStatus(reservaId, 'CANCELADA').subscribe(() => this.carregarDados());
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  formatHour(isoStr: string | null): string {
    if (!isoStr) return '';
    return new Date(isoStr).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }
}
```

#### `dashboard.component.html`
```html
<div class="dashboard-container">
  <header class="app-header">
    <div class="brand">
      <span class="logo">🍽️</span>
      <h1>MesaLive Painel</h1>
      <span class="badge role">{{ usuario?.role }}</span>
    </div>
    <div class="user-info">
      <span>Olá, <strong>{{ usuario?.nome }}</strong></span>
      <button class="btn-logout" (click)="logout()">Sair 🚪</button>
    </div>
  </header>

  <!-- ESTATÍSTICAS -->
  <div class="stats-row">
    <div class="stat-card total"><h3>Total de Mesas</h3><p class="number">{{ mesas.length }}</p></div>
    <div class="stat-card livre"><h3>Livres</h3><p class="number">{{ countLivre }}</p></div>
    <div class="stat-card reservada"><h3>Reservadas</h3><p class="number">{{ countReservada }}</p></div>
    <div class="stat-card ocupada"><h3>Ocupadas</h3><p class="number">{{ countOcupada }}</p></div>
    <div class="stat-card noshow"><h3>No-Show</h3><p class="number">{{ countNoShow }}</p></div>
  </div>

  <div class="loading-overlay" *ngIf="isLoading && mesas.length === 0">
    <div class="spinner"></div><p>Carregando mapa...</p>
  </div>

  <!-- MAPA DE MESAS -->
  <div class="mesas-grid" *ngIf="mesas.length > 0">
    <div class="mesa-card" *ngFor="let mesa of mesas" [ngClass]="mesa.statusAtual.toLowerCase()">
      <div class="card-header">
        <span class="mesa-title">{{ mesa.numero }}</span>
        <span class="capacity">👥 {{ mesa.capacidade }} lugares</span>
      </div>
      <div class="card-body">
        <div class="status-badge-container"><span class="status-badge">{{ mesa.statusAtual }}</span></div>
        <div class="reserva-details" *ngIf="mesa.reservaAtivaId">
          <div class="reserva-info"><strong>Cliente:</strong> {{ mesa.nomeCliente }}</div>
          <div class="reserva-info"><strong>Horário:</strong> {{ formatHour(mesa.dataHoraReserva) }}</div>
        </div>
        <div class="reserva-details empty" *ngIf="!mesa.reservaAtivaId && mesa.ativo">Disponível</div>
        <div class="reserva-details inactive" *ngIf="!mesa.ativo">Fora de serviço</div>
      </div>
      <div class="card-actions">
        <button class="action-btn toggle" (click)="toggleAtivo(mesa)" [class.active]="mesa.ativo">
          {{ mesa.ativo ? 'Desativar' : 'Reativar' }}
        </button>
        <ng-container *ngIf="mesa.ativo && mesa.reservaAtivaId">
          <button *ngIf="mesa.statusAtual === 'RESERVADA'" class="action-btn checkin" (click)="marcarEntrada(mesa.reservaAtivaId)">Entrada 👍</button>
          <button *ngIf="mesa.statusAtual === 'RESERVADA'" class="action-btn noshow" (click)="marcarNoShow(mesa.reservaAtivaId)">No-Show ⏱️</button>
          <button *ngIf="mesa.statusAtual === 'OCUPADA' || mesa.statusAtual === 'NO_SHOW'" class="action-btn checkout" (click)="liberarMesa(mesa.reservaAtivaId)">Liberar 🧹</button>
        </ng-container>
      </div>
    </div>
  </div>
</div>
```

#### `dashboard.component.css`
```css
.dashboard-container {
  min-height: 100vh; background: radial-gradient(circle at 10% 20%, rgb(4, 15, 34) 0%, rgb(18, 5, 29) 90.2%);
  font-family: 'Outfit', 'Inter', sans-serif; color: #f1f5f9; padding: 30px 40px;
}
.app-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); padding-bottom: 20px; }
.brand { display: flex; align-items: center; gap: 12px; }
.brand h1 { font-size: 1.8rem; font-weight: 700; margin: 0; background: linear-gradient(135deg, #a78bfa 0%, #ec4899 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.logo { font-size: 2.2rem; }
.badge.role { background: rgba(192, 132, 252, 0.15); color: #c084fc; border: 1px solid rgba(192, 132, 252, 0.3); font-size: 0.75rem; font-weight: 700; padding: 4px 10px; border-radius: 12px; text-transform: uppercase; }
.user-info { display: flex; align-items: center; gap: 20px; }
.btn-logout { background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); color: #fca5a5; padding: 10px 18px; border-radius: 12px; font-weight: 600; cursor: pointer; }
.btn-logout:hover { background: rgba(239, 68, 68, 0.2); border-color: #ef4444; }
.stats-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 20px; margin-bottom: 40px; }
.stat-card { background: rgba(255, 255, 255, 0.02); border: 1px solid rgba(255, 255, 255, 0.05); border-radius: 16px; padding: 20px; text-align: center; }
.stat-card h3 { font-size: 0.85rem; color: #94a3b8; margin: 0 0 10px 0; text-transform: uppercase; }
.stat-card .number { font-size: 2.2rem; font-weight: 700; margin: 0; }
.stat-card.livre .number { color: #4ade80; }
.stat-card.reservada .number { color: #a78bfa; }
.stat-card.ocupada .number { color: #60a5fa; }
.stat-card.noshow .number { color: #fbbf24; }
.mesas-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 25px; }
.mesa-card { background: rgba(255, 255, 255, 0.02); border: 1px solid rgba(255, 255, 255, 0.06); border-radius: 20px; overflow: hidden; display: flex; flex-direction: column; transition: all 0.3s ease; }
.mesa-card:hover { transform: scale(1.02); }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 20px 10px 20px; }
.mesa-title { font-size: 1.25rem; font-weight: 700; }
.capacity { font-size: 0.85rem; color: #94a3b8; background: rgba(255, 255, 255, 0.04); padding: 4px 8px; border-radius: 8px; }
.card-body { padding: 20px; flex: 1; display: flex; flex-direction: column; gap: 15px; }
.status-badge { font-size: 0.75rem; font-weight: 700; padding: 4px 10px; border-radius: 12px; text-transform: uppercase; border: 1px solid transparent; }
.mesa-card.livre { border-color: rgba(74, 222, 128, 0.15); }
.mesa-card.livre .status-badge { background: rgba(74, 222, 128, 0.1); color: #4ade80; }
.mesa-card.reservada { border-color: rgba(167, 139, 250, 0.15); }
.mesa-card.reservada .status-badge { background: rgba(167, 139, 250, 0.1); color: #a78bfa; }
.mesa-card.ocupada { border-color: rgba(96, 165, 250, 0.15); }
.mesa-card.ocupada .status-badge { background: rgba(96, 165, 250, 0.1); color: #60a5fa; }
.mesa-card.no_show { border-color: rgba(251, 191, 36, 0.15); }
.mesa-card.no_show .status-badge { background: rgba(251, 191, 36, 0.1); color: #fbbf24; }
.mesa-card.inativa { opacity: 0.5; }
.reserva-details { background: rgba(0, 0, 0, 0.2); border-radius: 12px; padding: 15px; font-size: 0.9rem; }
.reserva-info strong { color: #c084fc; }
.card-actions { display: grid; grid-template-columns: 1fr; gap: 10px; padding: 20px; background: rgba(0, 0, 0, 0.1); }
.action-btn { border: none; border-radius: 10px; padding: 10px; font-size: 0.85rem; font-weight: 600; cursor: pointer; text-align: center; }
.action-btn.toggle { background: rgba(255, 255, 255, 0.04); color: #cbd5e1; border: 1px solid rgba(255, 255, 255, 0.08); }
.action-btn.toggle.active { background: rgba(239, 68, 68, 0.08); color: #fca5a5; }
.action-btn.toggle:not(.active) { background: rgba(74, 222, 128, 0.08); color: #a7f3d0; }
.action-btn.checkin { background: #10b981; color: #fff; }
.action-btn.noshow { background: #f59e0b; color: #fff; }
.action-btn.checkout { background: #3b82f6; color: #fff; }
```

---

## 🎯 5. Integrando o Projeto no IntelliJ IDEA

Para configurar o workspace perfeitamente no seu IntelliJ e garantir a compilação:

1. **Abra o IntelliJ IDEA**.
2. Vá em **Open** (ou *Import*) e selecione a pasta raiz **`MesaLive`**.
3. Crie (ou edite) os seguintes arquivos sob a pasta `.idea/`:
   
   * **`.idea/gradle.xml`**:
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <project version="4">
       <component name="GradleSettings">
         <option name="linkedExternalProjectsSettings">
           <GradleProjectSettings>
             <option name="externalProjectPath" value="$PROJECT_DIR$/backend" />
             <option name="gradleJvm" value="#USE_PROJECT_JDK" />
             <option name="modules">
               <set>
                 <option value="$PROJECT_DIR$/backend" />
               </set>
             </option>
           </GradleProjectSettings>
         </option>
       </component>
     </project>
     ```

   * **`.idea/misc.xml`**:
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <project version="4">
       <component name="ProjectRootManager" version="2" project-jdk-name="17" project-jdk-type="JavaSDK">
         <output url="file://$PROJECT_DIR$/out" />
       </component>
     </project>
     ```

4. O IntelliJ detectará o backend Gradle e carregará todas as dependências automaticamente.

---

## 🚀 6. Executando e Testando a Aplicação Completa

### Etapa 1: Subir o PostgreSQL local (Docker)
Na pasta raiz `MesaLive`:
```bash
docker-compose up -d
```

### Etapa 2: Executar testes do backend
Na pasta `MesaLive/backend/`:
```bash
./gradlew test --tests "com.mesalive.service.ReservaServiceTest"
```
*(Deve retornar BUILD SUCCESSFUL).*

### Etapa 3: Executar o backend
Na pasta `MesaLive/backend/`:
```bash
./gradlew bootRun
```
* O backend subirá na porta `8080`.
* Acesse e teste os endpoints interativamente no Swagger em: `http://localhost:8080/swagger-ui/index.html`

### Etapa 4: Executar o frontend
Na pasta `MesaLive/frontend/`:
```bash
npm start
```
* A aplicação abrirá em `http://localhost:4200`.
* Crie reservas e use a **Área de Staff** (Login: `admin@mesalive.com` / Senha: `admin123`) para visualizar a sincronização ao vivo via WebSockets!
