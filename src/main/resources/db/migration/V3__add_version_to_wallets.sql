-- Adiciona coluna de versão para controle de concorrência otimista
ALTER TABLE wallets ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Adiciona um comentário sobre o propósito da coluna
COMMENT ON COLUMN wallets.version IS 'Controle de versão para evitar problemas de concorrência';
