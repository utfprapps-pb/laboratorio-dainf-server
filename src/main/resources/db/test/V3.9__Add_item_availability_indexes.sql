-- Índice para empréstimos (H2 não requer partial index em testes)
-- Beneficia filtro: "LEFT JOIN Emprestimo e ... AND e.dataDevolucao IS NULL"
CREATE INDEX IF NOT EXISTS idx_emprestimo_active_items
ON emprestimo(data_devolucao, id);

-- Índice composto para lookup de itens em empréstimos
-- Beneficia JOIN: "LEFT JOIN EmprestimoItem ei ON ei.item.id = i.id"
-- Otimiza agregação: "COALESCE(SUM(ei.qtde), 0)"
CREATE INDEX IF NOT EXISTS idx_emprestimo_item_active_lookup
ON emprestimo_item(item_id, emprestimo_id, qtde);
