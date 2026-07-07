<p align="center">
  <img src="frontend/assets/logo.png" alt="MesaLive — Gestão de Salão e Reservas em Tempo Real" width="400"/>
</p>

<p align="center">
  <strong>Sistema inteligente e reativo para controle de reservas de mesas em restaurantes.</strong><br/>
  Desenvolvido com Spring Boot 3 + Kotlin no backend e Angular 18 Standalone + Signals no frontend, integrado com WebSockets para sincronização de dados instantânea.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/status-em%20desenvolvimento-yellow" alt="Status"/>
  <img src="https://img.shields.io/badge/backend-Kotlin%20%2B%20Spring%20Boot%203-7F52FF" alt="Kotlin & Spring Boot"/>
  <img src="https://img.shields.io/badge/database-PostgreSQL-4169E1" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/realtime-WebSockets-010101" alt="WebSockets"/>
  <img src="https://img.shields.io/badge/frontend-Angular%2018-DD0031" alt="Angular 18"/>
  <img src="https://img.shields.io/badge/security-JWT-000000" alt="JWT Security"/>
</p>

---

## O Projeto

O **MesaLive** foi projetado para eliminar o atrito operacional de restaurantes de médio e pequeno porte que gerenciam reservas em cadernos físicos ou planilhas estáticas. Esse tipo de gestão gera gargalos como duplicidade de reservas e lentidão na comunicação da equipe de atendimento.

Com o MesaLive:
1. **O Cliente** faz agendamentos rápidos (sem necessidade de cadastros demorados), podendo consultar ou cancelar de forma 100% autônoma.
2. **O Staff (Garçons e Gerentes)** ganha um painel reativo que reflete o mapa do salão em tempo real. Qualquer reserva feita pelo cliente ou alteração realizada no salão é propagada de forma instantânea para todos os funcionários.

---

## Features Implementadas

### Portal do Cliente  
* **Agendamento Veloz:** Reserva em poucos cliques especificando quantidade de pessoas, data/horário e mesa desejada.
* **Prevenção Inteligente de Conflitos:** Algoritmo que impede agendamentos na mesma mesa dentro de uma janela de **90 minutos** de segurança (antes ou depois).
* **Gestão de Reserva:** O cliente pode buscar o status ou cancelar sua reserva de forma autônoma inserindo o código e o telefone.
* **LGPD Compliant:** A listagem pública de mesas exibe apenas a capacidade das mesas disponíveis, ocultando qualquer dado pessoal de outros clientes.

### Painel do Staff  
* **Mapa de Status em Tempo Real:** Grid interativo que classifica o status das mesas automaticamente:
  *  **Livre:** Sem reservas agendadas na janela de horário.
  *  **Reservada:** Cliente agendado com chegada prevista na janela atual.
  *  **Ocupada:** Clientes ativos no local (check-in realizado).
  *  **No-Show:** Tolerância de chegada de 15 minutos excedida.
  *  **Inativa:** Fora de serviço.
* **Controles Operacionais:** Ações rápidas de um clique para registrar "Entrada" (Check-in), "No-Show" (Não comparecimento) e "Liberar" (Check-out/limpeza da mesa).
* **Gestão de Atividade:** Habilitar ou desabilitar mesas físicas do fluxo público instantaneamente.

### 🔐 Segurança e Gestão de Funcionários  
* **Controle de Acessos:** Autenticação baseada em JWT stateless com papéis definidos (`ROLE_GERENTE` e `ROLE_GARCOM`).
* **Cadastro de Funcionários:** Painel administrativo integrado na tela do Gerente para cadastrar novos funcionários com encriptação de senha BCrypt automática.

---

## 🛠️ Stack Tecnológica

### Backend (Java 17 + Kotlin)
* **Spring Boot 3.x** & **Spring Security** (Segurança stateless baseada em JWT).
* **Spring Data JPA** & **Hibernate** (Mapeamento objeto-relacional).
* **PostgreSQL 15** (Persistência e integridade referencial de dados).
* **Flyway Migration** (Gerenciamento de histórico de banco de dados).
* **Spring WebSockets + STOMP / SockJS** (Broker de comunicação bidirecional).
* **MockK** & **JUnit 5** (Suite de testes unitários).
* **Testcontainers** (Ambiente de testes integrados reais com Docker).
* **OpenAPI / Swagger** (Documentação de APIs interativa).

### Frontend (Angular 18+)
* **Standalone Components** (Arquitetura moderna sem arquivos `NgModule`).
* **Angular Signals** (Gerenciador reativo de estados de autenticação).
* **RxJS** & **HttpClient** (Fluxo de dados e chamadas assíncronas REST).
* **SockJS Client** & **STOMPJS** (Sincronização persistente em tempo real).

---

## 🏛️ Padrões de Projeto & Arquitetura

O sistema implementa uma **Arquitetura em Camadas** com uma separação lógica estrita:
* **DTO Pattern:** Todos os payloads de API de entrada e saída usam objetos DTO específicos, blindando as entidades JPA do banco de dados de exposição acidental.
* **Strategy Pattern:** Lógica de validação de mesas encapsulada sob a interface `DisponibilidadeValidator` facilitando a criação de novas regras de restrição de salão sem alterar o serviço principal.
* **Observer Pattern:** Desacoplamento de ações no banco de dados do envio de eventos de rede. Salvar uma reserva dispara um `ReservaAlteradaEvent` via Spring que é processado assincronamente pelo `ReservaEventListener` para transmissão via WebSocket.
* **Global Exception Handler:** Interceptador de erros centralizado que formata as exceções de negócio em mensagens HTTP padronizadas com a classe `ErrorResponseDTO`.

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
* Ter o **Docker** e **Docker Desktop** rodando.
* Ter o **JDK 17** instalado e configurado nas variáveis de ambiente.
* Ter o **Node.js** instalado.

### Passo 1: Subir o Banco de Dados (Docker)
Na pasta raiz do projeto:
```bash
docker-compose up -d
```
*(Isso levantará o banco PostgreSQL na porta `5432` do Windows).*

### Passo 2: Executar o Backend (Spring Boot)
Entre na pasta `backend/` e execute o comando:
```bash
./gradlew bootRun
```
* O backend rodará em `http://localhost:8080`.
* A documentação interativa das APIs estará em: `http://localhost:8080/swagger-ui.html`.

### Passo 3: Executar o Frontend (Angular)
Abra outro terminal, vá para a pasta `frontend/`, instale as dependências e inicie o servidor:
```bash
cd frontend
npm install
npm start
```
* O frontend rodará em `http://localhost:4200`.

---

## 🔑 Credenciais Padrão para Testes

Durante a migração automática (`V2__seed_initial_data.sql`), o banco é populado com as credenciais:

| Papel (Role) | E-mail de Acesso | Senha | Acesso |
| :--- | :--- | :--- | :--- |
| **Gerente** | `admin@mesalive.com` | `admin123` | Mapa do salão, ações de mesas/reservas e painel de cadastro de novos garçons. |
| **Garçom** | `joao@mesalive.com` | `admin123` | Mapa do salão e ações de mesas/reservas. |

---

## 🗺️ Endpoints Principais da API

| Método | Rota | Autenticação Exigida | Descrição |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/reservas` | Livre | Criar nova reserva de cliente. |
| `GET` | `/api/reservas/{id}` | Livre (Valida Telefone) | Consultar reserva do cliente. |
| `DELETE` | `/api/reservas/{id}` | Livre (Valida Telefone) | Cancelar reserva do cliente. |
| `GET` | `/api/mesas/publicas` | Livre | Listar capacidade das mesas ativas para agendamento. |
| `POST` | `/api/auth/login` | Livre | Autenticar usuário do staff e gerar token JWT. |
| `GET` | `/api/mesas` | `GERENTE`, `GARCOM` | Exibir o painel de mesas do salão em tempo real. |
| `PATCH` | `/api/mesas/{id}/status` | `GERENTE`, `GARCOM` | Ativar ou inativar uma mesa física do salão. |
| `PATCH` | `/api/reservas/{id}/status` | `GERENTE`, `GARCOM` | Atualizar status da reserva (Check-in / No-Show). |
| `POST` | `/api/usuarios` | `GERENTE` | Cadastrar novo funcionário no staff. |
| `WS` | `/ws` | Livre | Handshake WebSocket para conexões STOMP/SockJS. |

---

## 🧪 Rodando Testes Unitários

Para validar as regras de negócio centrais, rode o comando na pasta `backend/`:
```bash
./gradlew test
```

---

## 📂 Estrutura de Diretórios do Projeto

```text
MesaLive/
├── backend/
│   ├── src/main/kotlin/com/mesalive/
│   │   ├── config/          # Configurações do Spring (WebSockets, Security)
│   │   ├── controller/      # Controladores REST HTTP
│   │   ├── domain/          # Entidades de Negócio (JPA)
│   │   ├── dto/             # Objetos de Transferência de Dados
│   │   ├── event/           # Eventos de Domínio Internos
│   │   ├── exception/       # Tratador Global de Erros
│   │   ├── mapper/          # Conversores Entidade <-> DTO
│   │   ├── repository/      # Interfaces de Comunicação com o PostgreSQL
│   │   ├── security/        # Filtros de Segurança JWT e Autenticação
│   │   ├── service/         # Interfaces e Lógicas de Negócio
│   │   └── websocket/       # Receptor de Eventos e Broker de Websocket
│   └── src/main/resources/
│       ├── db/migration/    # Scripts SQL do Flyway (V1 e V2)
│       └── application.properties
├── frontend/
│   └── src/app/
│       ├── components/      # Telas (Cliente, Login, Staff Dashboard)
│       └── services/        # Consumo de APIs REST, Auth (Signals) e WebSockets
├── docs/                    # Documentação oficial de Arquitetura e Features
├── docker-compose.yml       # Banco PostgreSQL
└── README.md
```

---

## 📚 Documentação Adicional do Projeto

Para informações detalhadas do projeto, consulte a documentação oficial na pasta `/docs`:

* **[Documentação de Arquitetura](./docs/architecture.md):** Padrões de projeto utilizados (JPA, DTO, Strategy, Observer) e fluxo reativo de WebSockets.
* **[Documentação Técnica (Onboarding)](./docs/technical.md):** Detalhamento de pacotes, rotas REST, modelagem de banco de dados PostgreSQL e métodos de teste (incluindo o cliente de teste HTML).
* **[Especificação de Funcionalidades (Features)](./docs/features.md):** Listagem detalhada de todas as regras de negócio de cliente/staff e o roadmap de funcionalidades futuras.
