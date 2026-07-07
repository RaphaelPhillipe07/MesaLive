  # Listagem de Features do Sistema  

Este documento descreve as funcionalidades implementadas no **MesaLive** e suas respectivas regras de validação.

---

## 1. Área do Cliente (Autoatendimento)

### Feature 1.1: Solicitação de Nova Reserva
* **Descrição:** O cliente escolhe uma data, horário, mesa específica e a quantidade de pessoas para agendar um almoço/jantar.
* **Regras de Negócio e Validações:**
  * Nome do cliente e telefone de contato são campos obrigatórios.
  * A data e hora da reserva devem estar obrigatoriamente no futuro.
  * O número de pessoas deve ser no mínimo 1 e não pode ultrapassar a capacidade máxima cadastrada na mesa selecionada.
  * A mesa desejada deve estar ativa para receber reservas.
  * **Prevenção de Conflitos:** O sistema impede reservas na mesma mesa caso já exista outra reserva confirmada em uma janela de 90 minutos de distância (para mais ou para menos).

### Feature 1.2: Consulta de Reserva Existente
* **Descrição:** Permite ao cliente conferir os detalhes de seu agendamento.
* **Regras de Negócio:**
  * O cliente deve informar o código numérico da reserva e o telefone de contato cadastrado.
  * Se o número de telefone não coincidir com o armazenado na reserva, o sistema bloqueia a consulta para proteger a privacidade do cliente.

### Feature 1.3: Cancelamento de Reserva pelo Cliente
* **Descrição:** Dá autonomia para o cliente cancelar seu compromisso.
* **Regras de Negócio:**
  * Exige a validação do código de reserva e do telefone cadastrado.
  * Altera o status da reserva para `CANCELADA`, liberando imediatamente a mesa para outros agendamentos e enviando um alerta ao painel do staff.

---

## 2. Área do Staff (Gerenciamento do Salão)

### Feature 2.1: Autenticação Administrativa (Staff Login)
* **Descrição:** Acesso protegido por credenciais de funcionários.
* **Regras de Negócio:**
  * Acesso restrito via autenticação stateless JWT.
  * As senhas dos usuários são armazenadas em hash BCrypt altamente seguro.
  * A API valida os papéis (`Role`) e garante acesso apenas a perfis autorizados (`GERENTE` ou `GARCOM`).

### Feature 2.2: Mapa do Salão em Tempo Real
* **Descrição:** Grade interativa contendo todas as mesas do restaurante e seus respectivos status calculados ao vivo.
* **Estados Visuais das Mesas:**
  * **Livre:** Mesa sem nenhuma reserva confirmada na janela de horário corrente.
  * **Reservada:** Mesa com reserva confirmada para o horário atual ou nos próximos 30 minutos.
  * **Ocupada:** Clientes ativos no local (marcado check-in).
  * **No-Show:** O horário da reserva passou há mais de 15 minutos e os clientes não compareceram.
  * **Inativa:** Mesa fora de serviço (manutenção, faxina ou reserva especial).
* **Atualização Reativa (WebSockets):** Qualquer mudança efetuada por um funcionário ou cancelamento de cliente atualiza o salão de todos os funcionários conectados instantaneamente sem recarregar a tela.

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
* **Descrição:** Permite desativar temporariamente mesas físicas por motivos operacionais (ex: limpeza ou quebra de cadeiras).
* **Regras de Negócio:**
  * Ao desativar uma mesa, ela assume o status **Inativa** e deixa de aparecer na lista pública de mesas do cliente, impedindo novos agendamentos nela.

---

## 3. Recursos de Infraestrutura e Documentação

### Feature 3.1: Migrations Automáticas e Seed Data
* **Descrição:** Criação da estrutura de tabelas e injeção automática de dados de teste (6 mesas e 2 contas administrativas de funcionários) no primeiro boot da aplicação.

### Feature 3.2: Documentação de API Interativa (OpenAPI/Swagger)
* **Descrição:** Documentação automática contendo a listagem de todos os controllers HTTP e esquemas de dados da aplicação exposta na rota: `http://localhost:8080/swagger-ui.html`.

---

## 4. Roadmap (Features Futuras)

### Feature 4.1: Pedidos por Mesa (Ordens de Consumo)
* **Descrição:** Vincula o consumo de pratos e bebidas à mesa ocupada.
* **Regras de Negócio Planejadas:**
  * Lançamento de itens do cardápio diretamente na conta da mesa (efetuado por garçons ou via QR Code pelo cliente).
  * O status da mesa `OCUPADA` exibe o valor acumulado em tempo real.
  * O encerramento/checkout da mesa exige a conferência e fechamento da conta de consumo.

### Feature 4.2: Gestão de Mesas Físicas (Cadastro e Edição - CRUD)
* **Descrição:** Permite ao Gerente gerenciar o layout físico do salão diretamente pelo painel.
* **Regras de Negócio Planejadas:**
  * Criação de novas mesas especificando número único e capacidade de assentos.
  * Edição de capacidade de mesas existentes (bloqueada se houver reservas confirmadas ativas na mesa).
  * Exclusão lógica ou física de mesas do restaurante.

### Feature 4.3: Compartilhar Comandas e Observações entre Garçons (WSS)
* **Descrição:** Compartilhamento em tempo real de notas, observações e alterações de comandas de mesa entre toda a equipe ativa de garçons via WebSockets.
* **Regras de Negócio Planejadas:**
  * Adicionar observações rápidas a uma mesa (ex: "cliente alérgico a camarão", "esperando acompanhante").
  * Sincronização em tempo real de mensagens de chat ou avisos operacionais entre os dispositivos do staff logados.
  * Atualização instantânea de qualquer alteração de itens na comanda.

### Feature 4.4: Painel KDS para Cozinha
* **Descrição:** Tela dedicada para a equipe da cozinha/copa acompanhar os pedidos lançados nas mesas em tempo real.
* **Regras de Negócio Planejadas:**
  * Divisão de pedidos por status (na fila, preparando, pronto para entrega).
  * Garçom recebe notificação instantânea quando o prato da mesa é marcado como "pronto".


