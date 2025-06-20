-- Adiciona coluna de versão para controle de concorrência otimista na tabela transactions
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS version BIGINT;

-- Atualiza quaisquer registros que possam ter version NULL para 0
UPDATE transactions SET version = 0 WHERE version IS NULL;

-- Reforça a restrição NOT NULL
ALTER TABLE transactions ALTER COLUMN version SET NOT NULL;

-- Adiciona comentário explicativo
COMMENT ON COLUMN transactions.version IS 'Controle de versão para evitar problemas de concorrência e garantir locking otimista';
