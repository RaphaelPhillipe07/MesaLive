# Relatório de Implementação Final - MesaLive 🍽️

Este documento detalha o processo completo de implementação do projeto **MesaLive**, cobrindo o backend (Spring Boot + Kotlin), o frontend (Angular 18) e as integrações de tempo real.

---

## 📁 Estrutura de Arquivos Final

O projeto está totalmente estruturado e configurado:
```text
MesaLive/
├── .idea/                           # Configurações do IntelliJ IDEA
│   ├── gradle.xml                   # Vínculo automático do backend como projeto Gradle
│   ├── misc.xml                     # Configuração do JDK do projeto (ajustado para Java 17)
│   └── ...
├── backend/                         # Backend (Kotlin + Spring Boot 3)
│   ├── build.gradle.kts             # Dependências atualizadas (Swagger, JWT, MockK, Testcontainers)
│   ├── gradlew / gradlew.bat        # Gradle Wrapper
│   └── src/
│       ├── main/
│       │   ├── kotlin/com/mesalive/
│       │   │   ├── config/          # Configurações (SecurityConfig, WebSocketConfig)
│       │   │   ├── controller/      # API REST (ReservaController, MesaController, AuthController)
│       │   │   ├── domain/          # Entidades JPA (Mesa, Cliente, Reserva, Usuario)
│       │   │   ├── dto/             # DTOs de entrada/saída e validações
│       │   │   ├── event/           # Eventos Spring (ReservaAlteradaEvent)
│       │   │   ├── mapper/          # Extensões Kotlin para conversão Entity <-> DTO
│       │   │   ├── repository/      # Interfaces de dados (MesaRepository, etc.)
│       │   │   ├── security/        # Serviços JWT e filtros de autenticação
│       │   │   ├── service/         # Interfaces e implementações de regras de negócio
│       │   │   └── websocket/       # Listeners para publicar no STOMP Broker
│       │   └── resources/
│       │       ├── application.properties
│       │       └── db/migration/
│       │           ├── V1__create_initial_schema.sql
│       │           └── V2__seed_initial_data.sql
│       └── test/                    # Testes Unitários
│           └── kotlin/com/mesalive/
│               ├── service/
│               │   └── ReservaServiceTest.kt # Testes com MockK
│               └── BackendApplicationTests.kt
├── frontend/                        # Frontend (Angular 18)
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   │   ├── dashboard/       # Painel do Staff (atualização WebSocket ao vivo)
│   │   │   │   ├── login/           # Login administrativo JWT
│   │   │   │   └── reserva-cliente/ # Tela pública de agendamento e cancelamentos
│   │   │   ├── services/
│   │   │   │   ├── api.service.ts
│   │   │   │   ├── auth.service.ts
│   │   │   │   └── websocket.service.ts
│   │   │   ├── app.config.ts
│   │   │   ├── app.routes.ts
│   │   │   └── app.component.ts
│   │   └── index.html
│   └── package.json
├── docker-compose.yml               # PostgreSQL local
└── README.md
```

---

## 🛠️ Detalhamento da Arquitetura Implementada

### 1. Modelo de Domínio e Banco de Dados (Flyway)
* **Entidades JPA:** `Mesa` (capacidade, número), `Cliente` (nome, contato), `Reserva` (status, dataHora) e `Usuario` (nome, hash, role) implementadas de forma imutável e com tipos seguros.
* **Migrations SQL:**
  * **V1:** Tabelas relacionais criadas com chaves primárias `BIGSERIAL` e relações parametrizadas.
  * **V2:** Carga inicial com 6 mesas e 2 usuários administrativos padrão (`admin@mesalive.com` e `joao@mesalive.com` - senha: `admin123`).

### 2. Validação e Concorrência (Strategy Pattern)
* Criada a interface `DisponibilidadeValidator` injetada de forma genérica como lista.
* A classe `ConflitoHorarioValidator` implementa o bloqueio de sobreposições: não permite agendamentos na mesma mesa dentro de uma janela de **90 minutos** antes ou depois de outra reserva ativa.

### 3. Padrão Observer e Desacoplamento de WebSocket
* O fluxo de atualização funciona de forma desacoplada:
  1. O `ReservaService` processa as ações e publica um `ReservaAlteradaEvent` via `ApplicationEventPublisher`.
  2. O `ReservaEventListener` captura o evento e propaga via `SimpMessagingTemplate` ao broker WebSocket no tópico `/topic/salao`.
  3. Isso mantém a lógica de banco de dados 100% isolada da lógica de redes/comunicação em tempo real.

### 4. Segurança de Dados e LGPD
* **Divisão de Endpoints de Mesas:**
  * Para evitar a exposição de nomes de clientes e detalhes de reservas a estranhos, criamos um endpoint público **`GET /api/mesas/publicas`** que retorna apenas o número e a capacidade das mesas.
  * O endpoint administrativo completo **`GET /api/mesas`** (que calcula o status de Livre, Reservada, Ocupada, No-Show) é protegido e exige token JWT de Staff (roles `GERENTE` ou `GARCOM`).

### 5. Frontend Angular 18 (Signals, Standalone & Websockets)
* O frontend foi desenhado com visual premium utilizando **Glassmorphism**, fontes importadas (*Outfit* e *Inter*), gradientes suaves de cor roxa/escura e micro-animações nas interações.
* **Signals:** Utilizados para o gerenciamento de estado de autenticação reativa (`AuthService.currentUser`).
* **Websockets:** O `WebsocketService` escuta em tempo real o canal `/topic/salao` utilizando SockJS e o protocolo STOMP. Qualquer alteração feita por um garçom se reflete no painel de todos os outros dispositivos conectados sem precisar atualizar a página.

---

## 🧪 Sucesso dos Testes Unitários

Executamos o conjunto de testes unitários para a validação da criação e bloqueio de conflitos na criação de reservas.
* **Ferramentas:** JUnit 5 e MockK.
* **Cenários Testados:**
  * Criação de reserva válida (Sucesso).
  * Tentativa em mesas inativas (`MesaInativaException`).
  * Tentativa com quantidade de pessoas superior à capacidade da mesa (`CapacidadeMesaInsuficienteException`).
* **Comando Executado:**
  ```powershell
  ./gradlew test --tests "com.mesalive.service.ReservaServiceTest"
  ```
* **Resultado:** **`BUILD SUCCESSFUL`** (Todos os testes compilaram e passaram com sucesso).

---

## 🚀 Como Executar o Projeto Completo

### A. Banco de Dados (Docker)
Inicie o banco local na porta `5432`:
```bash
docker-compose up -d
```

### B. Inicializar o Backend (Porta 8080)
Na pasta `/backend`, execute:
```bash
./gradlew bootRun
```
* O Swagger estará acessível para testes das APIs em: `http://localhost:8080/swagger-ui/index.html`

### C. Inicializar o Frontend (Porta 4200)
Na pasta `/frontend`, execute:
```bash
npm start
```
* Acesse a tela de cliente em `http://localhost:4200`
* Para acessar o painel de Staff, clique em **Área do Staff** e faça login com:
  * **E-mail:** `admin@mesalive.com`
  * **Senha:** `admin123`
