<p align="center">
  <img src="frontend/assets/logo.png" alt="MesaLive — Gestão de Salão e Reservas em Tempo Real" width="400"/>
</p>

<p align="center">
  <strong>Sistema inteligente e reativo para controle de reservas de mesas em restaurantes.</strong><br/>
  Desenvolvido com Spring Boot 3 + Kotlin no backend e Angular 18 Standalone + Signals + Smart Polling no frontend, apresentando uma interface limpa, minimalista e altamente compatível com o mercado.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/status-em%20desenvolvimento-yellow" alt="Status"/>
  <img src="https://img.shields.io/badge/backend-Kotlin%20%2B%20Spring%20Boot%203-7F52FF" alt="Kotlin & Spring Boot"/>
  <img src="https://img.shields.io/badge/database-PostgreSQL-4169E1" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/architecture-Smart%20Polling%20%2B%20Optimistic%20UI-10B981" alt="Smart Polling"/>
  <img src="https://img.shields.io/badge/frontend-Angular%2018%20%2B%20Signals-DD0031" alt="Angular 18"/>
  <img src="https://img.shields.io/badge/security-JWT%20Stateless-000000" alt="JWT Security"/>
</p>

---

## O Projeto

O **MesaLive** foi projetado para eliminar o atrito operacional de restaurantes de médio e pequeno porte que gerenciam reservas em cadernos físicos ou planilhas estáticas. Esse tipo de gestão gera gargalos como duplicidade de reservas e lentidão na comunicação da equipe de atendimento.

Com o MesaLive:
1. **O Cliente** faz agendamentos rápidos selecionando visualmente a mesa desejada em um grid interativo (sem necessidade de cadastros demorados), podendo consultar ou cancelar de forma 100% autônoma.
2. **O Staff (Garçons e Gerentes)** ganha um painel reativo que reflete o mapa do salão com sincronização constante via **Smart Polling** e respostas **instantâneas (UI Otimista)** para qualquer ação realizada no salão.

---

## Features Implementadas

### Portal do Cliente  
* **Agendamento Visual & Veloz:** Reserva em poucos cliques com **seletor visual de mesas em grid de cards interativos**, especificando quantidade de pessoas, data/horário e mesa preferida.
* **Prevenção Inteligente de Conflitos:** Algoritmo no backend que impede agendamentos na mesma mesa dentro de uma janela de segurança (antes ou depois).
* **Gestão Autônoma de Reserva:** O cliente pode buscar o status ou cancelar sua reserva inserindo o código e o telefone cadastrado.
* **LGPD Compliant:** A listagem pública de mesas exibe apenas o número e a capacidade das mesas disponíveis, ocultando qualquer dado pessoal de outros clientes.

### Painel do Staff  
* **Mapa do Salão com Smart Polling:** Grid interativo sincronizado automaticamente a cada 8 segundos (com pausa inteligente quando a aba está em segundo plano) que classifica o status das mesas:
  * **Livre:** Sem reservas agendadas na janela de horário.
  * **Reservada:** Cliente agendado com chegada prevista na janela atual.
  * **Ocupada:** Clientes ativos no local (check-in realizado).
  * **No-Show:** Tolerância de chegada excedida.
  * **Inativa:** Fora de serviço.
* **Ações Otimistas (Sensação Instantânea):** Atualizações visuais em menos de 10ms ao clicar em "Entrada" (Check-in), "No-Show" e "Liberar" (Check-out/limpeza da mesa).
* **Gestão de Atividade:** Habilitar ou desabilitar mesas físicas do fluxo público instantaneamente.

### 🔐 Segurança e Gestão de Funcionários  
* **Controle de Acessos:** Autenticação baseada em JWT stateless com papéis definidos (`ROLE_GERENTE` e `ROLE_GARCOM`).
* **HttpInterceptor Automático:** Injeção automática de tokens Bearer JWT em requisições autenticadas.
* **Cadastro de Funcionários:** Painel administrativo exclusivo para o Gerente cadastrar novos funcionários com encriptação de senha BCrypt.

---

## 🛠️ Stack Tecnológica

### Backend (Java 17 + Kotlin)
* **Spring Boot 3.x** & **Spring Security** (Segurança stateless baseada em JWT).
* **Spring Data JPA** & **Hibernate** (Mapeamento objeto-relacional).
* **PostgreSQL 15** (Persistência e integridade referencial de dados).
* **Flyway Migration** (Gerenciamento de histórico de banco de dados).
* **MockK** & **JUnit 5** (Suíte de testes unitários).
* **OpenAPI / Swagger** (Documentação interativa de APIs).

### Frontend (Angular 18+)
* **Standalone Components** (Arquitetura moderna sem arquivos `NgModule`).
* **Angular Signals & Computeds** (Gerenciamento reativo de estado de alta performance).
* **RxJS Smart Polling** (Sincronização assíncrona periódica a cada 8s com validação de visibilidade da página).
* **HTTP Interceptor** (Injeção transparente de credenciais JWT em requisições protegidas).
* **Design System Clean & Mercado** (Tipografia com Manrope, Space Grotesk e DM Mono, superfícies suaves, cantos arredondados e layout minimalista sem ícones/emojis poluídos).

---

## 🏛️ Padrões de Projeto & Arquitetura

O sistema implementa uma **Arquitetura em Camadas** com uma separação lógica estrita:
* **DTO Pattern:** Todos os payloads de API usam objetos DTO específicos e tipados tanto no Kotlin quanto no TypeScript (`Mesa`, `Reserva`, `Usuario`).
* **Strategy Pattern:** Lógica de validação de disponibilidade encapsulada sob a interface `DisponibilidadeValidator`.
* **Smart Polling & Optimistic UI:** Substituição da complexidade de conexões mantidas por WebSockets por chamadas HTTP REST limpas e atualização visual local instantânea.
* **Global Exception Handler:** Interceptador de erros centralizado no Spring Boot formatando exceções em respostas HTTP padronizadas.

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
* Ter o **Docker** e **Docker Desktop** rodando.
* Ter o **JDK 17** instalado.
* Ter o **Node.js** e **npm** instalados.

### Passo 1: Subir o Banco de Dados (Docker)
Na pasta raiz do projeto:
```bash
docker-compose up -d
```
*(Inicia o banco PostgreSQL na porta `5432`).*

### Passo 2: Executar o Backend (Spring Boot)
Entre na pasta `backend/` e execute:
```bash
cd backend
./gradlew bootRun
```
* O backend rodará em `http://localhost:8080`.
* Documentação das APIs (Swagger): `http://localhost:8080/swagger-ui.html`.

### Passo 3: Executar o Frontend (Angular)
Em outro terminal, acesse a pasta `frontend/` e inicie o servidor:
```bash
cd frontend
npm install
npm start
```
* O frontend rodará em `http://localhost:4200`.

---

## 🔑 Credenciais Padrão para Testes

| Papel (Role) | E-mail de Acesso | Senha | Permissões |
| :--- | :--- | :--- | :--- |
| **Gerente** | `admin@mesalive.com` | `admin123` | Mapa do salão, ações de mesas/reservas e cadastro de staff. |
| **Garçom** | `joao@mesalive.com` | `admin123` | Mapa do salão e ações de mesas/reservas. |

---

## 🗺️ Endpoints Principais da API

| Método | Rota | Autenticação Exigida | Descrição |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/reservas` | Livre | Criar nova reserva de cliente. |
| `GET` | `/api/reservas/{id}` | Livre (Valida Telefone) | Consultar reserva do cliente. |
| `DELETE` | `/api/reservas/{id}` | Livre (Valida Telefone) | Cancelar reserva do cliente. |
| `GET` | `/api/mesas/publicas` | Livre | Listar mesas ativas para o seletor visual de agendamento. |
| `POST` | `/api/auth/login` | Livre | Autenticar usuário do staff e gerar token JWT. |
| `GET` | `/api/mesas` | `GERENTE`, `GARCOM` | Exibir o painel de mesas do salão em tempo real. |
| `PATCH` | `/api/mesas/{id}/status` | `GERENTE`, `GARCOM` | Ativar ou inativar uma mesa física do salão. |
| `PATCH` | `/api/reservas/{id}/status` | `GERENTE`, `GARCOM` | Atualizar status da reserva (Check-in / No-Show / Liberar). |
| `POST` | `/api/usuarios` | `GERENTE` | Cadastrar novo funcionário no staff. |

---

## 📂 Estrutura de Diretórios Atualizada

```text
MesaLive/
├── backend/
│   ├── src/main/kotlin/com/mesalive/
│   │   ├── config/          # Configurações do Spring (Security, CORS)
│   │   ├── controller/      # Controladores REST HTTP
│   │   ├── domain/          # Entidades de Negócio (JPA)
│   │   ├── dto/             # Objetos de Transferência de Dados
│   │   ├── exception/       # Tratador Global de Erros
│   │   ├── mapper/          # Conversores Entidade <-> DTO
│   │   ├── repository/      # Interfaces de Comunicação com PostgreSQL
│   │   ├── security/        # Filtros de Segurança JWT e Autenticação
│   │   └── service/         # Interfaces e Lógicas de Negócio
│   └── src/main/resources/
│       ├── db/migration/    # Scripts SQL do Flyway
│       └── application.properties
├── frontend/
│   └── src/app/
│       ├── components/      # Componentes (ReservaCliente, Login, Dashboard)
│       ├── interceptors/    # HttpInterceptor para autorização Bearer JWT
│       ├── models/          # Interfaces TypeScript (Mesa, Reserva, Usuario)
│       └── services/        # Consumo de APIs REST e Auth com Signals
├── presentation/            # Referência de Design System, tokens e guias visuais
├── docs/                    # Documentação oficial de Arquitetura e Features
├── docker-compose.yml       # Banco PostgreSQL
└── README.md
```
