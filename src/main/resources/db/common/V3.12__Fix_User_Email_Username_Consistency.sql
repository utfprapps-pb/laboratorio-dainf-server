-- Migration V3.1 - Correção de consistência entre username e email
--
-- PROBLEMA: Vulnerabilidade de segurança onde sistema removia subdomínios de email UTFPR
-- durante autenticação, permitindo acesso cruzado entre contas.
--
-- EXEMPLO: vinicius@professores.utfpr.edu.br podia acessar vinicius@utfpr.edu.br
--
-- SOLUÇÃO: Garantir que username seja sempre o email completo para evitar colisões

-- Verificar usuários afetados pela inconsistência username/email
-- Note: Esta seção é apenas para documentação/diagnóstico

-- 1. Identificar usuários com username diferente do email
-- SELECT id, nome, username, email
-- FROM usuario
-- WHERE username != email;

-- 2. Identificar potenciais colisões (mesmo username para emails diferentes)
-- SELECT username, COUNT(*) as count, array_agg(email) as emails
-- FROM usuario
-- GROUP BY username
-- HAVING COUNT(*) > 1;

-- 3. Identificar usuários com domínios UTFPR afetados pela mudança
-- SELECT id, nome, username, email
-- FROM usuario
-- WHERE email LIKE '%@utfpr.edu.br'
--    OR email LIKE '%@professores.utfpr.edu.br'
--    OR email LIKE '%@administrativo.utfpr.edu.br';

-- CORREÇÕES ESPECÍFICAS para dados iniciais:

-- Corrigir usuário Vinicius Pegorini (ID 3 nos dados iniciais)
-- Antes: username='vinicius', email='vinicius@professores.utfpr.edu.br'
-- Depois: username='vinicius@professores.utfpr.edu.br', email='vinicius@professores.utfpr.edu.br'
UPDATE usuario
SET username = email
WHERE username != email
  AND email IS NOT NULL;

-- Adicionar índice para performance de busca por email/username
CREATE INDEX IF NOT EXISTS idx_usuario_username_email ON usuario(username, email);

-- Adicionar índice único para garantir consistência futura
-- Note: Não pode adicionar constraint UNIQUE direta devido a possíveis dados históricos
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_username_unique ON usuario(username)
WHERE username IS NOT NULL;

-- Documentação da mudança:
--
-- IMPACTO:
-- - Usuários afetados precisarão usar email completo para login
-- - Ex: 'vinicius' → 'vinicius@professores.utfpr.edu.br'
--
-- BENEFÍCIOS:
-- - Elimina vulnerabilidade de acesso cruzado entre domínios UTFPR
-- - Simplifica lógica de autenticação (usa username ou email completos)
-- - Melhora segurança e rastreabilidade
--
-- COMPATIBILIDADE:
-- - Sistema agora aceita tanto username quanto email no login
-- - Usuários existentes com usernames diferentes continuam funcionando
-- - Novos usuários terão username = email por padrão

COMMIT;