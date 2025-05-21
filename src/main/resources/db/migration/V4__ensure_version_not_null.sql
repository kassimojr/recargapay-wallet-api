-- Atualiza quaisquer registros que possam ter version NULL para 0
UPDATE wallets SET version = 0 WHERE version IS NULL;

-- Reforça a restrição NOT NULL
ALTER TABLE wallets ALTER COLUMN version SET NOT NULL;

-- Adiciona comentário explicativo
COMMENT ON COLUMN wallets.version IS 'Controle de versão para evitar problemas de concorrência, não pode ser nulo';
