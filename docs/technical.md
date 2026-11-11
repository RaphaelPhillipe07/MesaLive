# MesaLive — Documentação Técnica  

> Guia de onboarding para desenvolvedores. Explica a arquitetura, stack, fluxos de dados e
> convenções do projeto MesaLive.  

---

## 1. Visão Geral

MesaLive é um sistema inteligente de **gestão de reservas de mesas e mapa de salão em tempo real** para restaurantes. O objetivo é eliminar erros operacionais como overbooking e no-shows através de um fluxo unificado e sincronização constante. Dois atores principais interagem com o ecossistema:

| Ator | Aplicação | Tecnologia |
|---|---|---|
| **Cliente** | Portal de Autoatendimento (`frontend/`) | Angular 18 Standalone — SPA com Seletor Visual de Mesas |
| **Staff (Garçom / Gerente)** | Painel Administrativo (`frontend/dashboard`) | Angular 18 Standalone — Signals + Smart Polling + UI Otimista |

### Monorepo  

```text
MesaLive/
├── backend/              # Kotlin + Spring Boot 3 + PostgreSQL (Gradle)
│   ├── src/main/kotlin/  # Código-fonte da API REST HTTP
│   └── src/main/resources/
│       ├── db/migration/ # Migrations Flyway (V1 e V2)
│       └── application.properties
├── frontend/             # Angular 18 Standalone Components + Signals
│   ├── src/app/
│   │   ├── components/   # Componentes da UI (Login, Cliente, Dashboard)
│   │   ├── interceptors/ # HttpInterceptor para injeção automática de Bearer JWT
│   │   ├── models/       # Interfaces DTO TypeScript (Mesa, Reserva, Usuario)
│   │   └── services/     # Serviços (API com HttpClient, Auth com Signals)
│   └── src/styles.css    # Tokens globais do Design System SaaS Clean
├── docs/
│   ├── architecture.md   # Decisões de design arquitetural
│   ├── features.md       # Inventário de regras e funcionalidades
│   └── technical.md      # Este documento
└── docker-compose.yml    # PostgreSQL de desenvolvimento
```

---

## 2. Stack Tecnológica

### Backend (`backend/`)

| Camada | Tecnologia |
|---|---|
| Framework | Spring Boot 4.x + Kotlin JVM 21 |
| Build Tool | Gradle (Kotlin DSL) |
| Acesso a dados | Spring Data JPA + Hibernate |
| Banco de dados | PostgreSQL 15 |
| Migrations | Flyway |
| Segurança | Spring Security + Autenticação Stateless baseada em JWT |
| Senhas | Criptografia BCrypt |
| Documentação | Springdoc OpenAPI (Swagger UI) |
| Validação | Jakarta Validation (Bean Validation) |
| Testes Unitários | JUnit 5 + MockK |

### Frontend (`frontend/`)

| Camada | Tecnologia |
|---|---|
| Framework | Angular 18.x Standalone |
| Estado Global | Angular Signals & Computeds |
| Chamadas HTTP | HttpClient com HttpInterceptor para tokens Bearer JWT |
| Tipagem | TypeScript DTO Interfaces (`mesalive.models.ts`) |
| Sincronização | RxJS Smart Polling (timer 8s com detecção de visibilidade da aba) |
| Estilização | Vanilla CSS estruturado (Design System SaaS Clean) |
| Tipografia | Google Fonts (`Manrope` + `Space Grotesk` + `DM Mono`) |

---

## 3. Arquitetura do Sistema e Fluxo de Rede

```text
 ┌────────────────────────────────────────────────────────────┐
 │                    Rede Local / Cloud                      │
 │                                                            │
 │  ┌─────────────────┐           ┌────────────────────────┐  │
 │  │ Portal Cliente  │  HTTP     │ Backend (Spring Boot)  │  │
 │  │ (Angular SPA)   ├──────────►│ Porta 8080             │  │
 │  └─────────────────┘           │                        │  │
 │                                │ - Controllers REST     │  │
 │  ┌─────────────────┐  HTTP     │ - JPA + Flyway         │  │
 │  │ Dashboard Staff │  Polling  │ - Spring Security JWT  │  │
 │  │ (Signals / RxJS)├──────────►└───────────┬────────────┘  │
 │  └─────────────────┘                       │               │
 │                                            ▼ (Porta 5432)  │
 │                                ┌────────────────────────┐  │
 │                                │ Banco PostgreSQL       │  │
 │                                └────────────────────────┘  │
 └────────────────────────────────────────────────────────────┘
```

### Protocolos e Rotas

| Protocolo | Rota / Prefixo | Autenticação | Função |
|---|---|---|---|
| HTTP / REST | `/api/auth/login` | Livre | Autenticação do staff, retorna token JWT |
| HTTP / REST | `/api/mesas/publicas` | Livre | Listagem de mesas ativas para seletor visual em grid |
| HTTP / REST | `/api/reservas` | Livre | Criação, consulta e cancelamento pelo cliente |
| HTTP / REST | `/api/mesas/**` | JWT (Garçom/Gerente) | Mapa de salão administrativo detalhado |
| HTTP / REST | `/api/reservas/{id}/status`| JWT (Garçom/Gerente) | Ações de entrada, no-show e liberar mesa |
| HTTP / REST | `/api/usuarios` | JWT (Exclusivo Gerente) | Cadastro de novos funcionários no staff |

### Contrato de Erros

Todas as falhas usam o mesmo payload (`ErrorResponseDTO`: `status`, `error`, `message`, `timestamp`, `fieldErrors?`).

| Status | Situação |
|---|---|
| `400` | Regra de negócio violada, validação de campos, corpo/parâmetro malformado |
| `401` | Credenciais inválidas no login |
| `403` | Autenticado sem permissão para a ação |
| `404` | Recurso (ou endpoint) inexistente |
| `409` | Conflito de horário na mesa |
| `500` | Falha inesperada — a causa é logada no servidor, nunca devolvida ao cliente |

---

## 4. Banco de Dados (PostgreSQL)

### Modelagem de Tabelas

#### 1. `mesa`
* Armazena as mesas físicas do restaurante.
* **Campos:** `id` (PK), `numero` (Unique), `capacidade` (Int), `ativo` (Boolean).

#### 2. `cliente`
* Cadastro resumido de clientes que efetuaram reservas.
* **Campos:** `id` (PK), `nome` (String), `telefone` (String, pesquisável), `email` (String, opcional).

#### 3. `usuario`
* Contas administrativas de acesso para o staff.
* **Campos:** `id` (PK), `nome` (String), `email` (Unique), `senha_hash` (BCrypt Hash), `role` (`GERENTE` ou `GARCOM`).

#### 4. `reserva`
* Vincula mesas, clientes, datas e status operacionais.
* **Campos:** `id` (PK), `mesa_id` (FK), `cliente_id` (FK), `data_hora` (Timestamp with TZ), `data_hora_fim` (Timestamp with TZ), `quantidade_pessoas` (Int), `status` (`CONFIRMADA`, `CANCELADA`, `CLIENTE_CHEGOU`, `NO_SHOW`), `criado_em`, `atualizado_em`, `version`.
* **Constraints e índices (V3):**
  * `ex_reserva_sem_sobreposicao` — `EXCLUDE USING gist (mesa_id WITH =, tstzrange(data_hora, data_hora_fim) WITH &&) WHERE status IN ('CONFIRMADA','CLIENTE_CHEGOU')`. Impede double booking mesmo com requisições simultâneas.
  * `fk_reserva_mesa` / `fk_reserva_cliente` sem `ON DELETE CASCADE`: histórico não é apagado junto com mesa/cliente.
  * `ck_reserva_pessoas` (`> 0`) e `ck_reserva_intervalo` (`data_hora_fim > data_hora`).
  * `idx_reserva_mesa_data`, `idx_reserva_status_data`; `uq_cliente_telefone` em `cliente.telefone`.
* **Auditoria e concorrência:** `version` (`@Version`) aplica lock otimista; `criado_em`/`atualizado_em` são preenchidos por `@PrePersist`/`@PreUpdate`.
* **Mapa do salão:** uma única consulta (`ReservaRepository.findResumosParaPainel`) retorna mesas + reserva relevante via projeção, sem N+1 e sem carregar o histórico inteiro.

---

## 5. Estrutura de Código do Projeto

### Backend (Kotlin)
```text
com.mesalive.
├── config/
│   ├── SecurityConfig.kt       # Filtros JWT e permissões de rota REST
│   └── TimeConfig.kt           # Bean de Clock (relógio injetável)
├── controller/
│   ├── AuthController.kt       # Rota pública de login de funcionários
│   ├── MesaController.kt       # Endpoints de consulta de mesas (público/privado)
│   ├── ReservaController.kt    # CRUD de reservas e status
│   └── UsuarioController.kt    # Cadastro de usuários (restrito ao Gerente)
├── domain/
│   ├── Mesa.kt, Cliente.kt     # Entidades de persistência JPA
│   ├── Reserva.kt, Usuario.kt
│   ├── ReservaTempos.kt        # Duração e tolerâncias (fonte única)
│   └── ReservaStatusPolicy.kt  # Máquina de estados da reserva
├── dto/
│   ├── AuthDTOs.kt, MesaDTOs.kt# Payloads de entrada e saída (Request/Response)
│   ├── ReservaDTOs.kt, UsuarioDTOs.kt
│   └── ErrorResponseDTO.kt     # Payload padrão para erros globais
├── exception/
│   ├── Exceptions.kt           # Exceções de negócio
│   └── GlobalExceptionHandler.kt# Interceptador REST de exceções
├── mapper/
│   └── Mappers.kt              # Extensões Kotlin para conversão Entidade <-> DTO
├── repository/
│   └── JpaRepository interfaces# Contratos com banco de dados
├── security/
│   ├── JwtService.kt           # Geração e validação de tokens JWT
│   └── JwtAuthenticationFilter.kt# Filtro HTTP interceptador Bearer
└── service/
    ├── Interfaces de Serviços  # Contrato de serviços de negócio
    ├── impl/                   # Implementações (Reserva, Mesa, Cliente, Usuario, Salon)
    └── validator/
        ├── DisponibilidadeValidator.kt# Interface de validação de disponibilidade
        └── ConflitoHorarioValidator.kt# Implementação de checagem de janela
```

> **Testes:** unitários com JUnit 5 + MockK e integração com PostgreSQL embarcado
> (`io.zonky.test:embedded-postgres`), sem necessidade de Docker — as migrations Flyway,
> o `ddl-auto=validate` e o constraint de exclusão são exercitados em toda execução de `./gradlew test`.

### Frontend (Angular 18)
```text
frontend/src/app/
├── app.config.ts               # Provedores globais (HttpClient com Interceptor, Routes)
├── app.routes.ts               # Roteamento entre Cliente, Login e Dashboard
├── components/
│   ├── login/                  # Autenticação de staff
│   ├── reserva-cliente/        # Autoatendimento (Seletor visual em grid)
│   └── dashboard/              # Mapa do salão com Smart Polling e UI Otimista
├── interceptors/
│   └── auth.interceptor.ts     # Injeção automática de Bearer JWT
├── models/
│   └── mesalive.models.ts      # Interfaces DTOs TypeScript (Mesa, Reserva, Usuario)
└── services/
    ├── api.service.ts          # Chamadas REST HTTP tipadas
    └── auth.service.ts         # Controle de sessão usando Signals
```

---

## 6. Design System — SaaS Clean & Mercado

O visual do MesaLive adota um design system **SaaS Clean**, focado em ergonomia visual, alta legibilidade e minimalismo sem poluição de ícones/emojis.

### Paleta de Cores
* **Background (`--bg-app`):** `#F4F6F9` (Cinza Suave)
* **Card Background (`--bg-card`):** `#FFFFFF` (Branco Puro)
* **Texto Principal (`--text-main`):** `#132036` (Azul Marinho Escuro)
* **Texto Secundário (`--text-muted`):** `#59687a` (Cinza Azulado)
* **Ação Principal (`--primary`):** `#27466c` (Azul Marinho Corporativo)
* **Destaques de Status:**
  * **Livre:** Verde Esmeralda (`#10B981` / fundo `#ECFDF5`)
  * **Reservada:** Azul Marinho (`#27466C` / fundo `#E8F0FE`)
  * **Ocupada:** Vermelho Rubro (`#C92832` / fundo `#FEF2F2`)
  * **No-Show:** Dourado Quente (`#D8AB43` / fundo `#FFFBEB`)

### Tipografia
* **Headings:** *Space Grotesk* (Títulos e numeração de mesas)
* **Body:** *Manrope* (Leitura geral de formulários e rótulos)
* **Mono / Badges:** *DM Mono* (Códigos de confirmação e tags de permissão)

---

## 7. Comandos Úteis de Desenvolvimento

```bash
# Executar banco PostgreSQL de desenvolvimento
docker-compose up -d

# Executar testes unitários do backend
cd backend
./gradlew test

# Executar o backend em modo desenvolvimento
./gradlew bootRun

# Instalar dependências do frontend
cd frontend
npm install

# Executar o frontend em modo desenvolvimento (Porta 4200)
npm start

# Gerar build do frontend
npx ng build --configuration=development
```
