# MesaLive — Instruções para o Agente de Desenvolvimento

Este documento define como o projeto **MesaLive** deve ser estruturado, quais padrões seguir e o que é esperado em cada camada do sistema. Use este arquivo como referência viva durante todo o desenvolvimento.

---

## 1. Visão Geral do Projeto

Sistema de reservas de mesas para restaurantes com atualização em tempo real do status do salão (livre / reservada / ocupada / no-show), via WebSocket.

**Dois perfis de uso:**
- **Cliente**: faz reserva sem necessidade de login (nome + telefone + data/hora + nº de pessoas).
- **Staff (garçom/gerente)**: autenticado via JWT, gerencia o painel do salão e o status das mesas em tempo real.

---

## 2. Stack Técnica

| Camada | Tecnologia |
|---|---|
| Linguagem backend | Kotlin |
| Framework backend | Spring Boot 3.x |
| Persistência | Spring Data JPA + PostgreSQL |
| Migrations | Flyway |
| Tempo real | Spring WebSocket + STOMP sobre SockJS |
| Autenticação | Spring Security + JWT (apenas para staff) |
| Frontend | Angular 18+ (standalone components, Signals) |
| Cliente WebSocket | `@stomp/stompjs` + `sockjs-client` |
| Testes backend | JUnit 5, MockK, Spring Boot Test, Testcontainers (PostgreSQL) |
| Testes frontend | Jasmine/Karma (padrão Angular) ou Jest |
| Build | Gradle (Kotlin DSL) |
| Containerização | Docker + Docker Compose (app + Postgres) |
| Documentação de API | springdoc-openapi (Swagger UI) |

---

## 3. Arquitetura

### 3.1 Estilo arquitetural
Adotar **arquitetura em camadas (Layered Architecture)** com separação clara de responsabilidades, seguindo os princípios de **Clean Architecture** de forma pragmática (sem exagero de abstração para um projeto júnior — o objetivo é código organizado e testável, não over-engineering).

```
com.mesalive
├── config           -> configurações (Security, WebSocket, OpenAPI, CORS)
├── controller        -> camada de entrada HTTP (REST controllers)
├── dto                -> objetos de transferência (request/response), nunca expor entidades JPA direto
├── mapper             -> conversão entre Entity <-> DTO
├── service            -> regras de negócio (interfaces + implementação)
│   └── impl
├── repository         -> interfaces Spring Data JPA
├── domain / model     -> entidades JPA
├── exception          -> exceções customizadas + handler global (@ControllerAdvice)
├── websocket          -> handlers e configuração de eventos em tempo real
└── security           -> filtros JWT, provider de autenticação
```

**Regra de dependência:** `controller -> service -> repository`. Controller nunca acessa repository diretamente. Service nunca conhece detalhes de HTTP (não recebe `HttpServletRequest`, não retorna `ResponseEntity`).

### 3.2 Fluxo de uma reserva (exemplo prático)
1. `ReservaController` recebe `POST /api/reservas` com um `ReservaRequestDTO`
2. Valida o payload (`@Valid`, Bean Validation)
3. Delega para `ReservaService.criarReserva(dto)`
4. Service verifica conflito de horário/mesa (regra de negócio central do sistema)
5. Se ok, salva via `ReservaRepository`, converte para `ReservaResponseDTO`
6. Service publica evento no WebSocket (`SimpMessagingTemplate.convertAndSend("/topic/salao", statusAtualizado)`)
7. Controller retorna 201 com o DTO de resposta

---

## 4. Padrões de Projeto (Design Patterns) a Aplicar

| Padrão | Onde usar | Por quê |
|---|---|---|
| **DTO Pattern** | Toda entrada/saída de API | Nunca expor entidades JPA diretamente; desacopla API do modelo de dados |
| **Repository Pattern** | Acesso a dados | Já fornecido pelo Spring Data JPA — manter interfaces enxutas, métodos derivados (`findByDataAndMesaId`, etc.) |
| **Strategy Pattern** | Validação de disponibilidade de mesa | Interface `DisponibilidadeValidator` com implementação padrão; facilita trocar/estender regras futuramente (ex: regra de horário de cozinha) |
| **Builder Pattern** | Construção de DTOs/entidades complexas em testes | Usar Kotlin named arguments e default values; se necessário, criar `TestDataBuilder` para os testes |
| **Observer Pattern (via eventos Spring)** | Notificação de mudança de status de mesa | Usar `ApplicationEventPublisher` internamente e `SimpMessagingTemplate` para propagar ao WebSocket — desacopla a lógica de "salvar reserva" da lógica de "notificar quem está ouvindo" |
| **Factory Method (implícito)** | Criação de exceções de negócio | Métodos estáticos em exceções customizadas, ex: `ConflitoReservaException.paraMesa(mesaId, horario)` |
| **Singleton (via Spring Bean)** | Services, Repositories, Mappers | Padrão default do Spring (escopo singleton), não precisa configurar nada manualmente |

> Não force padrões desnecessários (ex: Factory complexo, Abstract Factory, Chain of Responsibility) só para "mostrar conhecimento". Clean Code preza por simplicidade e legibilidade acima de tudo.

---

## 5. Clean Code — Regras Práticas

- **Nomes em português ou inglês, mas consistentes** — não misturar (`Reserva` e `Reservation` no mesmo projeto, por exemplo). Recomendação: domínio em português (`Reserva`, `Mesa`, `Cliente`), termos técnicos em inglês (`Repository`, `Service`, `DTO`).
- **Funções pequenas, uma responsabilidade por método.** Se um método de service passa de ~20 linhas, considere quebrar.
- **Sem lógica de negócio em Controller.** Controller só orquestra: recebe request, chama service, retorna response.
- **Sem `null` como retorno de service** — usar `Optional<T>` no Java-style ou, preferencialmente em Kotlin, tipos nullable explícitos (`Mesa?`) e tratar com `?:` ou lançar exceção customizada.
- **Exceções de negócio customizadas**, nunca `RuntimeException` genérica. Ex: `MesaIndisponivelException`, `ReservaNaoEncontradaException`. Tratadas globalmente em `GlobalExceptionHandler` (`@RestControllerAdvice`), retornando um payload de erro padronizado (`ErrorResponseDTO`).
- **Injeção de dependência via construtor**, nunca `@Autowired` em campo. Em Kotlin, usar `constructor injection` direto na classe (aproveitar a sintaxe concisa do Kotlin).
- **Imutabilidade sempre que possível** — usar `data class` para DTOs, `val` em vez de `var` por padrão.
- **Validação na borda** — Bean Validation (`@NotBlank`, `@Future`, `@Min`, etc.) nos DTOs de request, não repetir validação manual no service quando já coberto por anotação.

---

## 6. Modelo de Domínio Inicial

### Entidades
- **Mesa**: `id`, `numero`, `capacidade`, `ativo`
- **Cliente**: `id`, `nome`, `telefone`, `email (opcional)`
- **Reserva**: `id`, `mesa`, `cliente`, `dataHora`, `quantidadePessoas`, `status` (enum: `CONFIRMADA`, `CANCELADA`, `CLIENTE_CHEGOU`, `NO_SHOW`)
- **Usuario** (staff): `id`, `nome`, `email`, `senhaHash`, `role` (enum: `GARCOM`, `GERENTE`)

### Regra de negócio central
Não pode haver duas reservas com status `CONFIRMADA` para a mesma mesa em horários que se sobreponham (considerar uma janela padrão de duração de reserva, ex: 90 minutos, configurável).

---

## 7. Endpoints Iniciais (MVP)

```
POST   /api/reservas                 -> criar reserva (público)
GET    /api/reservas/{id}             -> consultar reserva (público, via id + telefone)
DELETE /api/reservas/{id}             -> cancelar reserva (público, via telefone)

GET    /api/mesas                     -> listar mesas com status atual (staff)
PATCH  /api/mesas/{id}/status         -> atualizar status da mesa (staff, autenticado)

POST   /api/auth/login                -> login staff, retorna JWT

WS     /ws                            -> handshake WebSocket (SockJS)
STOMP  /topic/salao                   -> broadcast de atualizações do salão
```

---

## 8. Estratégia de Testes

**Pirâmide de testes esperada:**
1. **Testes unitários (maioria)** — Services e validadores de disponibilidade, usando MockK para mockar repositories. Foco na regra de conflito de horário.
2. **Testes de integração** — Controllers com `@SpringBootTest` + `MockMvc`, usando Testcontainers com PostgreSQL real (evitar H2 para não mascarar diferenças de comportamento com o banco de produção).
3. **Teste do fluxo WebSocket** — pelo menos um teste validando que, ao criar uma reserva, uma mensagem é publicada no tópico `/topic/salao`.

**Convenção de nomes de teste:** `deveRetornarConflito_quandoMesaJaReservadaNoHorario()` (padrão `deve<Resultado>_quando<Condicao>`).

**Cobertura mínima esperada:** regras de negócio (validação de conflito, cancelamento, mudança de status) devem ter 100% de cobertura de cenários (caminho feliz + pelo menos 2 cenários de erro cada).

---

## 9. Ordem Sugerida de Implementação

1. Setup do projeto (Gradle Kotlin DSL, dependências, Docker Compose com Postgres)
2. Entidades + migrations Flyway
3. Repositories + testes de repository (opcional, Spring Data já é testado)
4. Service de Mesa e Cliente (CRUD básico) + testes unitários
5. Service de Reserva com regra de conflito + testes unitários (é o coração do sistema, priorizar aqui)
6. Controllers + DTOs + Bean Validation + testes de integração
7. Exception handler global
8. WebSocket config + publicação de eventos ao criar/atualizar reserva
9. Autenticação JWT para staff
10. Frontend Angular: tela de reserva do cliente
11. Frontend Angular: painel do salão com conexão WebSocket em tempo real
12. Docker Compose final unindo backend + frontend + banco

---

## 10. Fora de Escopo (não implementar ainda)

Fila de espera automática, lembretes por SMS/email, integração com sistemas de pagamento, multi-restaurante (multi-tenant). Essas ficam como "próximos passos" documentados no README, não como parte do MVP.
