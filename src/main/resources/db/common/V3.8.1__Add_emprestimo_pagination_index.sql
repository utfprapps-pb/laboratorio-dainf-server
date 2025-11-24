-- Índice composto para padrões comuns de paginação
-- Suporta ORDER BY id DESC, data_emprestimo DESC
-- Benefício: PostgreSQL pode usar index-only scan para paginação
CREATE INDEX IF NOT EXISTS idx_emprestimo_pagination_id_date
  ON emprestimo(id DESC, data_emprestimo DESC);

-- Nota: PostgreSQL 11+ suporta INCLUDE clause
CREATE INDEX IF NOT EXISTS idx_emprestimo_pagination_covering
  ON emprestimo(id DESC)
  INCLUDE (data_emprestimo, prazo_devolucao, data_devolucao,
           usuario_emprestimo_id, usuario_responsavel_id);
