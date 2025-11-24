-- Migration V3.13 - Remover máscaras de telefones
--
-- OBJETIVO: Remover caracteres especiais (parênteses, hífens, espaços) dos números de telefone
-- armazenados na tabela fornecedor, mantendo apenas os dígitos numéricos.
--
-- EXEMPLO:
--   Antes: (41) 3123-4567, (41)98765-4321, 41 3123 4567
--   Depois: 4131234567, 41987654321, 4131234567

-- Atualizar telefones do fornecedor removendo caracteres não-numéricos
UPDATE fornecedor
SET telefone = REGEXP_REPLACE(telefone, '[^0-9]', '')
WHERE telefone IS NOT NULL
  AND REGEXP_LIKE(telefone, '[^0-9]'); -- Apenas atualiza se contiver caracteres não-numéricos

-- Comentário: Esta migração garante que todos os telefones sejam armazenados
-- em formato numérico puro, facilitando validações e formatações no frontend.
