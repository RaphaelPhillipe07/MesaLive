# Proposta de Aprendizado (Learning Proposal) 🧠

Esta proposta consolida os aprendizados práticos adquiridos durante a resolução de problemas nesta sessão, dividindo-os em regras e boas práticas para evitar erros similares no futuro.

---

## 1. Classificação dos Aprendizados

| ID | Tópico | Classificação | Descrição |
|---|---|---|---|
| **L1** | **Hoisting de Imports no Angular (SockJS)** | Regra de Desenvolvimento Web | Declarar variáveis globais necessárias para pacotes JS legados no `<head>` do `index.html` via script, e não no `main.ts`, para evitar erros de inicialização. |
| **L2** | **Interpolação de Strings no Kotlin (BCrypt)** | Regra de Desenvolvimento Backend | Escapar o caractere `$` (`\$`) em strings contendo hashes de senha no Kotlin para evitar erros de interpolação e referências não resolvidas. |
| **L3** | **Orquestração de Portas do Docker Compose** | Habilidade Operacional | Sempre inspecionar a vinculação de portas de host (`docker ps`) ao reiniciar containers e aplicar force-remove (`docker rm -f`) para evitar conflitos de portas não mapeadas. |

---

## 2. Detalhamento e Modificações Propostas

### 🛠️ L1: Regra para Variáveis Globais de Navegador (Angular / SPAs)
* **Problema:** Bibliotecas como `sockjs-client` dependem do objeto `global` (nativo do Node.js). Adicionar `(window as any).global = window;` no `main.ts` não funciona porque os comandos `import` são carregados e executados antes de qualquer código JS do arquivo (import hoisting).
* **Solução recomendada:** Adicionar uma regra de frontend para que, se um pacote precisar de polifill global, ele seja injetado direto no `<head>` do `index.html`.
* **Texto a ser adicionado nas instruções do desenvolvedor:**
  ```markdown
  * Se um projeto Angular ou SPA utilizar bibliotecas que dependam de variáveis globais do Node (como 'global' do sockjs-client), declare a variável global via tag `<script>` no topo do `<head>` no `index.html` para garantir que ela exista antes da avaliação dos módulos JavaScript (ESM).
  ```

### 🛠️ L2: Regra para Hashes no Kotlin (Escaping)
* **Problema:** Hashes BCrypt contêm cifrões (`$`). O Kotlin interpreta o `$` dentro de strings normais de aspas duplas como início de um template expression (ex: `$R` vira busca de variável `R`), quebrando a compilação.
* **Solução recomendada:** Regra estrita de Kotlin.
* **Texto a ser adicionado nas instruções do desenvolvedor:**
  ```markdown
  * Ao declarar strings contendo hashes de senha (como BCrypt que utilizam o caractere '$') em arquivos Kotlin (.kt), sempre escape todos os cifrões usando `\$` (ex: `\$2a\$10\$...`) para evitar erros de compilação por referência não resolvida (String template interpolation).
  ```

### 🛠️ L3: Habilidade de Resolução de Conexão de Banco no Docker
* **Problema:** O container PostgreSQL pode estar ativo, mas sem mapeamento de porta para o Windows (ex: apenas `5432/tcp` em vez de `0.0.0.0:5432->5432/tcp`), impedindo conexões locais do Spring Boot.
* **Solução recomendada:** Checklist de verificação rápida de infraestrutura local.
* **Texto a ser adicionado nas instruções:**
  ```markdown
  * Se o aplicativo falhar com "Connection refused" ao tentar se conectar ao banco de dados rodando em Docker:
    1. Execute `docker ps` e verifique a coluna PORTS.
    2. Se não contiver o mapeamento `0.0.0.0:5432->5432/tcp`, pare o container anterior com `docker rm -f <nome>` e execute `docker-compose down -v` antes de subir novamente com `docker-compose up -d`.
  ```

---

## 3. Próximos Passos
Após a sua revisão e aprovação deste artefato:
1. Posso salvar estas diretrizes no diretório de regras locais (`.gemini/`) ou documentá-las no nosso repositório para consulta em futuras tarefas.
