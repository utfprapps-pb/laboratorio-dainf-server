-- Índices na tabela de join ManyToMany
CREATE INDEX IF NOT EXISTS idx_usuario_permissoes_usuario_id
    ON usuario_permissoes(usuario_id);

CREATE INDEX IF NOT EXISTS idx_usuario_permissoes_permissoes_id
    ON usuario_permissoes(permissoes_id);

-- Índice B-tree em UPPER(nome) para busca case-insensitive
-- NOTA: H2 não suporta pg_trgm, usa B-tree como fallback
CREATE INDEX IF NOT EXISTS idx_usuario_nome_upper
    ON usuario (UPPER(nome));
