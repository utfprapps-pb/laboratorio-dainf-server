CREATE INDEX IF NOT EXISTS idx_emprestimo_status_dates
    ON emprestimo (data_devolucao, prazo_devolucao, data_emprestimo, id);

CREATE INDEX IF NOT EXISTS idx_emprestimo_usuario_status
    ON emprestimo (usuario_emprestimo_id, data_devolucao);

CREATE INDEX IF NOT EXISTS idx_emprestimo_prazo_notificacao
    ON emprestimo (data_devolucao, prazo_devolucao);

CREATE INDEX IF NOT EXISTS idx_emprestimo_abertos
    ON emprestimo (id, data_devolucao);
