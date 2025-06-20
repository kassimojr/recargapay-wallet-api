-- Remove a coluna de versão da tabela wallets
ALTER TABLE wallets DROP COLUMN IF EXISTS version;

-- Remove a coluna de versão da tabela transactions
ALTER TABLE transactions DROP COLUMN IF EXISTS version;
