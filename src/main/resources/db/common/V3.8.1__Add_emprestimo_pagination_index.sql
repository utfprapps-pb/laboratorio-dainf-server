-- ==============================================================================
-- Migration V3.1: Índice de Paginação para Emprestimo
-- Data: 2025-10-22
-- Objetivo:
--   Otimizar queries de paginação de emprestimo com índice composto
-- Melhoria esperada: 5-10% adicional em queries paginadas
-- ==============================================================================

-- Índice composto para padrões comuns de paginação
-- Suporta ORDER BY id DESC, data_emprestimo DESC
-- Benefício: PostgreSQL pode usar index-only scan para paginação
CREATE INDEX IF NOT EXISTS idx_emprestimo_pagination_id_date
  ON emprestimo(id DESC, data_emprestimo DESC);

-- Índice covering (inclui colunas adicionais frequentemente consultadas)
-- Permite queries evitarem acesso à tabela principal
-- Nota: PostgreSQL 11+ suporta INCLUDE clause
CREATE INDEX IF NOT EXISTS idx_emprestimo_pagination_covering
  ON emprestimo(id DESC)
  INCLUDE (data_emprestimo, prazo_devolucao, data_devolucao,
           usuario_emprestimo_id, usuario_responsavel_id);
