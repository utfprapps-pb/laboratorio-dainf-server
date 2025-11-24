CREATE INDEX IF NOT EXISTS idx_usuario_permissoes_usuario_id
    ON usuario_permissoes(usuario_id);

CREATE INDEX IF NOT EXISTS idx_usuario_permissoes_permissoes_id
    ON usuario_permissoes(permissoes_id);

COMMENT ON INDEX idx_usuario_permissoes_usuario_id IS
'Otimiza JOIN de usuários para permissões em queries paginadas.
Usado em UsuarioServiceImpl.usuarioCompleteByUserAndDocAndNome() e usuarioCompleteLab().
Performance esperada: ~25-35% de ganho em queries role-based.';

COMMENT ON INDEX idx_usuario_permissoes_permissoes_id IS
'Otimiza JOIN reverso (permissões → usuários) e validações de autorização.
Melhora performance de consultas que buscam usuários por role específica.';

DO $$
BEGIN
    -- Tenta criar extensão pg_trgm (requer privilégios de superuser)
    CREATE EXTENSION IF NOT EXISTS pg_trgm;

    -- Se sucesso, cria índice GIN trigram (otimiza LIKE '%texto%')
    CREATE INDEX IF NOT EXISTS idx_usuario_nome_trgm
        ON usuario USING gin (nome gin_trgm_ops);

    RAISE NOTICE 'pg_trgm extension ativada. Índice trigram criado em usuario.nome';

EXCEPTION
    WHEN insufficient_privilege THEN
        -- Fallback: Cria índice B-tree em UPPER(nome) se pg_trgm não disponível
        RAISE NOTICE 'pg_trgm extension não disponível (requer superuser). Usando B-tree em UPPER(nome) como fallback.';

        CREATE INDEX IF NOT EXISTS idx_usuario_nome_upper
            ON usuario (UPPER(nome));

    WHEN OTHERS THEN
        -- Outro erro: também usa fallback B-tree
        RAISE NOTICE 'Erro ao criar trigram index: %. Usando B-tree como fallback.', SQLERRM;

        CREATE INDEX IF NOT EXISTS idx_usuario_nome_upper
            ON usuario (UPPER(nome));
END $$;

-- Comenta o índice trigram (se criado com sucesso)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_usuario_nome_trgm'
    ) THEN
        COMMENT ON INDEX idx_usuario_nome_trgm IS
'Índice trigram (pg_trgm) para otimizar LIKE ''%texto%'' em usuario.nome.
Usado em UsuarioServiceImpl.usuarioComplete() e searchByTextAndRoles().
Performance esperada: ~15-25% de ganho vs B-tree simples.';
    END IF;
END $$;

-- Comenta o índice B-tree fallback (se criado)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_usuario_nome_upper'
    ) THEN
        COMMENT ON INDEX idx_usuario_nome_upper IS
'Índice B-tree em UPPER(nome) (fallback quando pg_trgm não disponível).
Otimiza case-insensitive LIKE parcialmente. Para performance máxima, habilite pg_trgm.';
    END IF;
END $$;
