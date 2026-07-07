# MesaLive — Documentação Técnica  

> Guia de onboarding para desenvolvedores. Explica a arquitetura, stack, fluxos de dados e
> convenções do projeto MesaLive.  

---

## 1. Visão Geral

MesaLive é um sistema inteligente de **gestão de reservas de mesas e mapa de salão em tempo real** para restaurantes. O objetivo é eliminar erros operacionais como overbooking e no-shows através de um fluxo unificado e sincronização instantânea. Dois atores principais interagem com o ecossistema:

| Ator | Aplicação | Tecnologia |
|---|---|---|
| **Cliente** | Portal de Autoatendimento (`frontend/`) | Angular 18 Standalone — SPA no Navegador |
| **Staff (Garçom / Gerente)** | Painel Administrativo (`frontend/dashboard`) | Angular 18 Standalone + Comunicação via WebSockets |

### Monorepo  

```
MesaLive/
├── backend/              # Kotlin + Spring Boot 3 + PostgreSQL (Gradle)
│   ├── src/main/kotlin/  # Código-fonte da API REST e WebSocket
│   └── src/main/resources/
│       ├── db/migration/ # Migrations Flyway (V1 e V2)
│       └── application.properties
├── frontend/             # Angular 18 Standalone Components + Signals
│   ├── src/app/
│   │   ├── components/   # Componentes da UI (Login, Cliente, Dashboard)
│   │   └── services/     # Serviços (API, Auth com Signals, WebSocket STOMP)
│   └── src/styles.css    # Tokens globais do Design System Luxury/Editorial
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
| Framework | Spring Boot 3.x + Kotlin JVM 17 |
| Build Tool | Gradle (Kotlin DSL) |
| Acesso a dados | Spring Data JPA + Hibernate |
| Banco de dados | PostgreSQL 15 |
| Migrations | Flyway |
| Segurança | Spring Security + Autenticação Stateless baseada em JWT |
| Senhas | BCrypt Criptografia |
| Real-time | Spring WebSockets (STOMP sobre SockJS) |
| Documentação | Springdoc OpenAPI (Swagger UI) |
| Validação | Jakarta Validation (Bean Validation) |
| Testes Unitários | JUnit 5 + MockK |
| Testes Integrados | Testcontainers PostgreSQL |

### Frontend (`frontend/`)

| Camada | Tecnologia |
|---|---|
| Framework | Angular 18.x Standalone |
| Estado Global | Angular Signals (Autenticação reativa) |
| Chamadas HTTP | HttpClient (provideHttpClient nativo) |
| Protocolo Tempo Real | `@stomp/stompjs` + `sockjs-client` |
| Estilização | Vanilla CSS estruturado  |
| Tipografia | Google Fonts (Playfair Display + Inter) |

---

## 3. Arquitetura do Sistema e Fluxo de Rede

```
 ┌────────────────────────────────────────────────────────────┐
 │                    Rede Local / Cloud                      │
 │                                                            │
 │  ┌─────────────────┐           ┌────────────────────────┐  │
 │  │ Portal Cliente  │  HTTP     │ Backend (Spring Boot)  │  │
 │  │ (Angular SPA)   ├──────────►│ Porta 8080             │  │
 │  └─────────────────┘           │                        │  │
 │                                │ - Controllers REST     │  │
 │  ┌─────────────────┐  WSS      │ - WebSocket Broker     │  │
 │  │ Dashboard Staff │◄─────────►│ - JPA + Flyway         │  │
 │  │ (Angular WS)    │  (STOMP)  └───────────┬────────────┘  │
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
| HTTP / REST | `/api/auth/login` | Livre | Autenticação do staff, retorna o token JWT |
| HTTP / REST | `/api/mesas/publicas` | Livre | Listagem segura de mesas ativas (sem dados sensíveis) |
| HTTP / REST | `/api/reservas` | Livre | Criação, consulta e cancelamento pelo cliente |
| HTTP / REST | `/api/mesas/**` | JWT (Garçom/Gerente) | Mapa de salão administrativo detalhado |
| HTTP / REST | `/api/usuarios` | JWT (Exclusivo Gerente) | Cadastro de novos funcionários no staff |
| WebSocket | `/ws` | Livre | Handshake inicial e fallback SockJS |
| STOMP Topic | `/topic/salao` | Livre | Broadcast de atualizações de reservas em tempo real |

### Fluxo de Eventos Desacoplados (Observer Pattern)
1. **Mutação:** O cliente faz um agendamento (`POST /api/reservas`) ou o staff altera o status (`PATCH /api/reservas/{id}/status`).
2. **Persistência:** O `ReservaServiceImpl` valida regras de negócio, salva no banco e dispara um `ReservaAlteradaEvent` via `ApplicationEventPublisher`.
3. **Broadcast:** O `ReservaEventListener` captura o evento de forma assíncrona, mapeia o domínio para DTO e envia via `SimpMessagingTemplate` para o canal WebSocket `/topic/salao`.
4. **Atualização Visual:** Todos os painéis administrativos conectados escutam a alteração e atualizam a UI sem necessidade de refresh manual.

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
* **Campos:** `id` (PK), `mesa_id` (FK), `cliente_id` (FK), `data_hora` (Timestamp with TZ), `quantidade_pessoas` (Int), `status` (`CONFIRMADA`, `CANCELADA`, `CLIENTE_CHEGOU`, `NO_SHOW`).

### Convenções do Banco
* **Migrations:** Nomeadas na pasta `resources/db/migration/` (ex: `V1__create_initial_schema.sql`).
* **Validações de Integridade:** `onDelete: 'cascade'` implementado nas chaves estrangeiras de mesa e cliente.
* **Fuso Horário:** Sempre armazene e trafegue fusos horários explícitos (`OffsetDateTime` / `TIMESTAMP WITH TIME ZONE`).

---

## 5. Estrutura de Código do Projeto

### Backend (Kotlin)
```
com.mesalive.
├── config/
│   ├── SecurityConfig.kt       # Filtros JWT e permissões de rota
│   └── WebSocketConfig.kt      # Configuração do Broker de WebSocket (STOMP)
├── controller/
│   ├── AuthController.kt       # Rota pública de login de funcionários
│   ├── MesaController.kt       # endpoints de consulta de mesas (público/privado)
│   ├── ReservaController.kt    # CRUD de reservas e status
│   └── UsuarioController.kt    # Cadastro de usuários (restrito ao Gerente)
├── domain/
│   ├── Mesa.kt, Cliente.kt     # Entidades de persistência JPA
│   └── Reserva.kt, Usuario.kt
├── dto/
│   ├── AuthDTOs.kt, MesaDTOs.kt# Payloads de entrada e saída (Request/Response)
│   ├── ReservaDTOs.kt, UsuarioDTOs.kt
│   └── ErrorResponseDTO.kt     # Payload padrão para erros globais
├── event/
│   └── ReservaAlteradaEvent.kt # Evento interno do Spring Framework
├── exception/
│   ├── Exceptions.kt           # Exceções de negócio (NegocioException e filhas)
│   └── GlobalExceptionHandler.kt# Interceptador REST de exceções
├── mapper/
│   └── Mappers.kt              # Extensões Kotlin para conversão Entidade ↔ DTO
├── repository/
│   └── JpaRepository interfaces# Contratos com banco de dados
├── security/
│   ├── JwtService.kt           # Geração e validação de tokens JWT
│   └── JwtAuthenticationFilter.kt# Filtro HTTP interceptador Bearer
├── service/
│   ├── Interfaces de Serviços  # Contrato de serviços de negócio
│   ├── impl/                   # Implementações dos serviços de negócio
│   └── validator/
│       ├── DisponibilidadeValidator.kt# Interface de validação de disponibilidade
│       └── ConflitoHorarioValidator.kt# Implementação (janela de 90 min)
└── websocket/
    └── ReservaEventListener.kt # Listener de eventos e broker WebSocket
```

### Frontend (Angular 18)
```
frontend/src/app/
├── app.config.ts               # Provedores globais (HTTP, Routes, Zone)
├── app.routes.ts               # Roteamento entre Cliente, Login e Dashboard
├── components/
│   ├── login/                  # Autenticação de staff
│   ├── reserva-cliente/        # Autoatendimento (Abas Nova / Minha Reserva)
│   └── dashboard/              # Mapa do salão com sincronização e ações rápidas
└── services/
    ├── api.service.ts          # Chamadas REST HTTP tipadas com injeção JWT
    ├── auth.service.ts         # Controle de sessão usando Signals
    └── websocket.service.ts    # Conexão STOMP/SockJS e fluxo Observável
```

---

## 6. Design System — Luxury / Editorial

O visual do MesaLive adota o design system **Luxury/Editorial**, caracterizado por minimalismo geométrico, tipografia proeminente e contraste de alta costura.

### Paleta de Cores
* **Background (`--bg`):** `#F9F8F6` (Alabastro Quente) — Fundo simulando papel ou linho.
* **Foreground (`--fg`):** `#1A1A1A` (Carvão Rico) — Tons escuros suaves em vez de preto puro para conforto visual.
* **Accent (`--accent`):** `#D4AF37` (Ouro Metálico) — Utilizado de forma comedida para foco, status ativo e hovers.
* **Muted Background (`--muted-bg`):** `#EBE5DE` (Taupe Pálido) — Para planos de fundo de tabelas, botões alternativos e cabeçalhos de abas.
* **Muted Foreground (`--muted-fg`):** `#6C6863` (Cinza Quente) — Para descrições e placeholders.

### Tipografia
* **Heading Font:** *Playfair Display* (Serifa de alto contraste). Utilizado em títulos grandes, numerações de estatísticas e itálicos de destaque.
* **Body Font:** *Inter* (Sans-serif humanista). Utilizado para labels, inputs, textos de leitura e botões.

### Detalhes de Assinatura Visual 
1. **Bordas Retangulares (0px):** Rigidez geométrica obrigatória. Sem cantos arredondados.
2. **Textura de Papel:** Camada fixa com opacidade 2% simulando grão de papel no CSS global.
3. **Grade Editorial:** Linhas de grade verticais em segundo plano criando um visual estruturado de revista impressa.
4. **Slide Dourado (Hover):** Botões primários possuem uma transição de 500ms onde um painel dourado desliza da esquerda para a direita ao passar o mouse.
5. **Letras Capitulares (Drop Caps):** O parágrafo de introdução do cliente utiliza capitularização gigante (4.2rem) para reforçar o estilo jornalístico.
6. **Inputs Underline:** Inputs e selects não possuem caixas ou bordas externas. Apenas uma linha fina inferior carvão que muda para ouro no foco.

---

## 7. Eventos do WebSocket (STOMP)

O canal ativo `/topic/salao` trafega objetos JSON contendo os dados tipados de atualizações de reservas sempre que há criação, cancelamento ou mudança de status:

```json
{
  "id": 5,
  "mesa": {
    "id": 3,
    "numero": "Mesa 3",
    "capacidade": 4,
    "ativo": true
  },
  "cliente": {
    "id": 12,
    "nome": "Carlos Silva",
    "telefone": "11999999999",
    "email": "carlos@exemplo.com"
  },
  "dataHora": "2026-07-07T20:00:00-03:00",
  "quantidadePessoas": 3,
  "status": "CONFIRMADA"
}
```

---

## 8. Ciclo de Vida do Salão (Status da Mesa)

O status visual de cada mesa exibida no painel administrativo (`DashboardComponent`) é computado dinamicamente no backend combinando o status da mesa física com a reserva correspondente no fuso horário atual:

* **INATIVA:** Se `mesa.ativo == false`.
* **OCUPADA:** Se existir uma reserva com status `CLIENTE_CHEGOU` ativa na mesa.
* **NO_SHOW:** Se existir uma reserva com status `NO_SHOW` ativa na data atual.
* **RESERVADA:** Se existir uma reserva confirmada para o dia atual e a hora atual estiver dentro da janela de tolerância de ocupação (30 minutos antes do início até 90 minutos após).
* **LIVRE:** Se nenhuma das condições anteriores for atendida.

---

## 9. Comandos Úteis de Desenvolvimento

```bash
# Executar banco PostgreSQL de testes/desenvolvimento
docker-compose up -d

# Executar testes unitários do backend
cd backend
./gradlew test

# Executar o backend em modo desenvolvimento (Live Reload)
./gradlew bootRun

# Instalar dependências do frontend
cd frontend
npm install

# Executar o frontend em modo desenvolvimento (Porta 4200)
npm start

# Gerar build de produção do frontend
npx ng build --configuration=production
```

---

## 10. Métodos de Teste e Integração

Para verificar o comportamento da API e da reatividade em tempo real, os desenvolvedores possuem três opções de teste:

### Opção A: Testes Unitários Automatizados
Execução de suite de testes do JUnit 5 com MockK na pasta `backend/`:
```bash
./gradlew test
```

### Opção B: Coleções de Teste em Clientes HTTP (Postman/Insomnia)
Utilização de coleções para disparar requisições REST nas rotas públicas e privadas (anexando o header `Authorization: Bearer <JWT>`).

### Opção C: Página HTML de Teste Isolada  
Uma página web estática completa foi criada diretamente no servidor backend em `backend/src/main/resources/static/api_test_client.html`. 

Quando o backend estiver rodando (`./gradlew bootRun`), você pode abrir o navegador diretamente em:
👉 **`http://localhost:8080/api_test_client.html`**

**Recursos disponíveis nesta página:**
1. **Autenticação Direta:** Insira e-mail e senha de staff para obter e salvar o token JWT.
2. **WebSocket Live Monitor:** Conecta ao canal `/topic/salao` e exibe logs das mensagens WebSockets recebidas ao vivo à medida que alterações ocorrem.
3. **Criação de Reservas:** Formulário rápido para simular o agendamento de clientes na data desejada.
4. **Painel de Ações Rápidas:** Executa listagens, check-ins, no-shows e bloqueio de mesas em lote sem precisar preencher dados complexos em JSON.
5. **Painel de Gerente:** Permite cadastrar novos funcionários enviando requisições criptografadas diretamente para o endpoint restrito do Spring Security.
