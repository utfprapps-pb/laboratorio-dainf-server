CREATE INDEX IF NOT EXISTS idx_emprestimo_status_dates
    ON emprestimo (data_devolucao, prazo_devolucao, data_emprestimo, id);

CREATE INDEX IF NOT EXISTS idx_emprestimo_usuario_status
    ON emprestimo (usuario_emprestimo_id, data_devolucao);

-- Os índices abaixo possuem WHERE por serem índices parciais, recurso que o pg suporta
-- Uso prático: Notificação busca itens que não foram devolvidos
-- Imagine 1000 registros, onde 20 são abertos, index parcial é 50x menor
CREATE INDEX IF NOT EXISTS idx_emprestimo_prazo_notificacao
    ON emprestimo (data_devolucao, prazo_devolucao)
    WHERE data_devolucao IS NULL;

CREATE INDEX IF NOT EXISTS idx_emprestimo_abertos
    ON emprestimo (id)
    WHERE data_devolucao IS NULL;
