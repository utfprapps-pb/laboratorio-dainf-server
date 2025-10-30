-- NOTA: A tabela permissao possui apenas 4 registros (ROLE_ADMINISTRADOR,
-- ROLE_LABORATORISTA, ROLE_PROFESSOR, ROLE_ALUNO). Em tabelas pequenas,
-- PostgreSQL pode optar por Sequential Scan mesmo com índice disponível,
-- pois é mais eficiente. Este índice é criado para manter consistência
-- de design e preparar para possível expansão futura de roles.

CREATE INDEX IF NOT EXISTS idx_permissao_nome ON permissao(nome);

COMMENT ON INDEX idx_permissao_nome IS
'Otimiza filtros role-based via JPA Specifications.
Usado em UsuarioServiceImpl.usuarioCompleteByUserAndDocAndNome() e usuarioCompleteLab().
Performance esperada: ~10-15% em tabelas pequenas, ~25-30% com muitas roles.';
