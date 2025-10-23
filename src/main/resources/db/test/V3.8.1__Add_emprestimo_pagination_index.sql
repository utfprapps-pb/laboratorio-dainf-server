-- ==============================================================================
-- Migration V3.1: Índice de Paginação para Emprestimo (H2 Compatible)
-- Data: 2025-10-22
-- Objetivo:
--   Otimizar queries de paginação de emprestimo com índice composto
-- Melhoria esperada: 5-10% adicional em queries paginadas
-- ==============================================================================

-- Índice composto para padrões comuns de paginação
-- H2 syntax: CREATE INDEX não suporta DESC diretamente
-- H2 ignora DESC na criação mas otimiza queries
CREATE INDEX IF NOT EXISTS idx_emprestimo_pagination_id_date
  ON emprestimo(id, data_emprestimo);

-- Nota: H2 não suporta INCLUDE clause (PostgreSQL 11+)
-- Apenas índice simples é criado no ambiente de teste
