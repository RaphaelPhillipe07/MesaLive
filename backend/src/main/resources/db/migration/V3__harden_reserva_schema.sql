-- V3: endurece o schema da reserva.
--   * fim explícito do intervalo da reserva
--   * exclusão de sobreposição por mesa no próprio banco (fim do double booking por corrida)
--   * índices para as consultas de conflito e do painel
--   * identidade de cliente por telefone
--   * integridade de domínio, auditoria e lock otimista
--   * histórico de reservas não é mais apagado em cascata

-- 1) Intervalo ocupado -------------------------------------------------------
-- Coluna persistida (e não expressão calculada) porque o EXCLUDE exige expressão imutável:
-- o operador timestamptz + interval é STABLE por causa de DST e não pode ser indexado.
ALTER TABLE reserva ADD COLUMN data_hora_fim TIMESTAMPTZ;
UPDATE reserva SET data_hora_fim = data_hora + INTERVAL '90 minutes';
ALTER TABLE reserva ALTER COLUMN data_hora_fim SET NOT NULL;

-- 2) Concorrência ------------------------------------------------------------
-- Duas reservas ativas na mesma mesa não podem se sobrepor, mesmo com requisições simultâneas.
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reserva ADD CONSTRAINT ex_reserva_sem_sobreposicao
    EXCLUDE USING gist (
        mesa_id WITH =,
        tstzrange(data_hora, data_hora_fim) WITH &&
    ) WHERE (status IN ('CONFIRMADA', 'CLIENTE_CHEGOU'));

-- 3) Índices de leitura ------------------------------------------------------
CREATE INDEX idx_reserva_mesa_data ON reserva (mesa_id, data_hora);
CREATE INDEX idx_reserva_status_data ON reserva (status, data_hora);

-- 4) Identidade do cliente ---------------------------------------------------
-- A deduplicação por telefone era feita só em código; agora o banco garante.
CREATE UNIQUE INDEX uq_cliente_telefone ON cliente (telefone);

-- 5) Integridade de domínio --------------------------------------------------
ALTER TABLE mesa ADD CONSTRAINT ck_mesa_capacidade CHECK (capacidade > 0);
ALTER TABLE reserva ADD CONSTRAINT ck_reserva_pessoas CHECK (quantidade_pessoas > 0);
ALTER TABLE reserva ADD CONSTRAINT ck_reserva_intervalo CHECK (data_hora_fim > data_hora);

-- 6) Auditoria e lock otimista ----------------------------------------------
ALTER TABLE reserva ADD COLUMN criado_em TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE reserva ADD COLUMN atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE reserva ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 7) Histórico não pode ser apagado em cascata -------------------------------
ALTER TABLE reserva DROP CONSTRAINT fk_reserva_mesa;
ALTER TABLE reserva ADD CONSTRAINT fk_reserva_mesa FOREIGN KEY (mesa_id) REFERENCES mesa(id);

ALTER TABLE reserva DROP CONSTRAINT fk_reserva_cliente;
ALTER TABLE reserva ADD CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id);
