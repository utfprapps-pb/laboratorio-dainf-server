-- Índices na tabela de join ManyToMany
CREATE INDEX IF NOT EXISTS idx_usuario_permissoes_usuario_id
    ON usuario_permissoes(usuario_id);

CREATE INDEX IF NOT EXISTS idx_usuario_permissoes_permissoes_id
    ON usuario_permissoes(permissoes_id);

-- Índice simples em nome (sem função) para ambiente de teste
-- H2 não suporta índices com função como UPPER(nome)
CREATE INDEX IF NOT EXISTS idx_usuario_nome
    ON usuario(nome);
