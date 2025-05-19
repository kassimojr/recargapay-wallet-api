-- Adicionar as colunas created_at e updated_at à tabela transactions
ALTER TABLE public.transactions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE public.transactions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Atualizar registros existentes com timestamp atual
UPDATE public.transactions SET created_at = NOW() WHERE created_at IS NULL;
UPDATE public.transactions SET updated_at = NOW() WHERE updated_at IS NULL;
