# Listagem de Features do Sistema  

Este documento descreve as funcionalidades implementadas no **MesaLive** e suas respectivas regras de validação.

---

## 1. Área do Cliente (Autoatendimento)

### Feature 1.1: Solicitação de Nova Reserva (Seletor Visual de Mesas)
* **Descrição:** O cliente escolhe uma data, horário, quantidade de pessoas e seleciona visualmente sua mesa preferida em um **grid interativo de cards de mesas**.
* **Regras de Negócio e Validações:**
  * Nome do cliente e telefone de contato são campos obrigatórios.
  * A data e hora da reserva devem estar obrigatoriamente no futuro.
  * O número de pessoas deve ser no mínimo 1 e não pode ultrapassar a capacidade máxima cadastrada na mesa selecionada.
  * A mesa desejada deve estar ativa para receber reservas.
  * **Prevenção de Conflitos:** O sistema impede reservas na mesma mesa caso já exista outra reserva confirmada em uma janela de segurança de distância.

### Feature 1.2: Consulta de Reserva Existente
* **Descrição:** Permite ao cliente conferir os detalhes de seu agendamento.
* **Regras de Negócio:**
  * O cliente deve informar o código numérico da reserva e o telefone de contato cadastrado.
  * Se o número de telefone não coincidir com o armazenado na reserva, o sistema bloqueia a consulta para proteger a privacidade do cliente.

### Feature 1.3: Cancelamento de Reserva pelo Cliente
* **Descrição:** Dá autonomia para o cliente cancelar seu compromisso.
* **Regras de Negócio:**
  * Exige a validação do código de reserva e do telefone cadastrado.
  * Altera o status da reserva para `CANCELADA`, liberando imediatamente a mesa para outros agendamentos.

---

## 2. Área do Staff (Gerenciamento do Salão)

### Feature 2.1: Autenticação Administrativa (Staff Login)
* **Descrição:** Acesso protegido por credenciais de funcionários com injeção automática de token Bearer via `HttpInterceptor`.
* **Regras de Negócio:**
  * Acesso restrito via autenticação stateless JWT.
  * As senhas dos usuários são armazenadas em hash BCrypt altamente seguro.
  * A API valida os papéis (`Role`) e garante acesso apenas a perfis autorizados (`GERENTE` ou `GARCOM`).

### Feature 2.2: Mapa do Salão com Smart Polling & UI Otimista
* **Descrição:** Grade interativa contendo todas as mesas do restaurante e seus respectivos status atualizados via polling de 8s (com pausa inteligente quando a aba é minimizada).
* **Estados Visuais das Mesas:**
  * **Livre:** Mesa sem nenhuma reserva confirmada na janela de horário corrente.
  * **Reservada:** Mesa com reserva confirmada para o horário atual ou próximo.
  * **Ocupada:** Clientes ativos no local (marcado check-in).
  * **No-Show:** O horário da reserva passou da tolerância e os clientes não compareceram.
  * **Inativa:** Mesa fora de serviço.
* **Respostas Otimistas:** Qualquer ação executada pelo garçom/gerente altera o estado visual da mesa instantaneamente (< 10ms), sincronizando a alteração com a API REST em segundo plano.

### Feature 2.3: Registro de Entrada (Check-in de Cliente)
* **Descrição:** Marca a chegada dos clientes no restaurante.
* **Regras de Negócio:**
  * Apenas para mesas no status `RESERVADA`.
  * Transiciona a reserva vinculada para o status `CLIENTE_CHEGOU` (a mesa passa a constar como **Ocupada**).

### Feature 2.4: Registro de No-Show (Não Comparecimento)
* **Descrição:** Sinaliza a ausência do cliente para liberar a mesa após tolerância de tempo.
* **Regras de Negócio:**
  * Transiciona a reserva para o status `NO_SHOW`. A mesa permanece com status amarelo temporariamente para controle do gerente e posterior liberação.

### Feature 2.5: Liberação de Mesa (Checkout)
* **Descrição:** Libera a mesa do salão após a saída dos clientes.
* **Regras de Negócio:**
  * Altera o status da reserva ativa para `CANCELADA` (ou finalizado), retornando a mesa ao estado de **Livre** no painel imediatamente.

### Feature 2.6: Controle de Atividade de Mesas (Manutenção)
* **Descrição:** Permite desativar temporariamente mesas físicas por motivos operacionais (ex: limpeza ou manutenção).
* **Regras de Negócio:**
  * Ao desativar uma mesa, ela assume o status **Inativa** e deixa de aparecer na lista pública de mesas do cliente, impedindo novos agendamentos nela.

### Feature 2.7: Cadastro de Staff pelo Gerente
* **Descrição:** Painel exclusivo na tela do Gerente para cadastrar novos garçons e gerentes diretamente pelo sistema.
* **Regras de Negócio:**
  * Exige role `GERENTE`.
  * Valida duplicidade de e-mail e salva a senha criptografada via BCrypt.

---

## 3. Recursos de Infraestrutura e Documentação

### Feature 3.1: Migrations Automáticas e Seed Data
* **Descrição:** Criação da estrutura de tabelas e injeção automática de dados de teste (mesas e contas administrativas) no boot da aplicação via Flyway.

### Feature 3.2: Documentação de API Interativa (OpenAPI/Swagger)
* **Descrição:** Documentação automática contendo a listagem de todos os controllers HTTP e esquemas de dados exposta na rota: `http://localhost:8080/swagger-ui.html`.

---

## 4. Roadmap (Features Futuras)

### Feature 4.1: Termômetro de Ocupação "Live" e Feed de Eventos
* **Descrição:** Exibição do percentual de ocupação ao vivo na página do cliente e feed com a linha do tempo de eventos recentes do salão no painel do staff.

### Feature 4.2: Pedidos por Mesa (Ordens de Consumo)
* **Descrição:** Vincula o consumo de pratos e bebidas à mesa ocupada.

### Feature 4.3: Gestão de Mesas Físicas (CRUD pelo Painel)
* **Descrição:** Permite ao Gerente cadastrar novas mesas, alterar capacidade de assentos e configurar layouts físicos diretamente pela interface.
