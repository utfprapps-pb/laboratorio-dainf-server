-- Migration V3.6: Sincroniza o campo 'ativo' com o status de 'email_verificado' na tabela usuario
-- Esta migration garante que o campo 'ativo' reflita corretamente o status de verificação de e-mail para todos os usuários existentes.
-- Se email_verificado = TRUE, então ativo = TRUE
-- Se email_verificado = FALSE, então ativo = FALSE

-- Atualiza usuários com e-mail verificado
UPDATE usuario SET ativo = TRUE WHERE email_verificado IS TRUE;

-- Atualiza usuários sem e-mail verificado
UPDATE usuario SET ativo = FALSE WHERE email_verificado IS FALSE;

-- Fim da migration: todos os registros de usuario tiveram o campo 'ativo' sincronizado com 'email_verificado'.
