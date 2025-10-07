-- ==============================================================================
-- Migration V3.0: Índices de Performance (Test Environment)
-- Data: 2025-10-07
-- Objetivo: Adicionar índices para otimizar queries mais frequentes
-- Melhoria esperada: 40-60% em queries filtradas
--
-- NOTA: Versão simplificada para H2 test environment
--       Não renomeia FKs (H2 não suporta DROP CONSTRAINT IF EXISTS)
--       FKs serão renomeadas apenas em dev/prod
-- ==============================================================================

-- Índices para tabela EMPRESTIMO
-- Suporta queries de dashboard e filtros por data
CREATE INDEX IF NOT EXISTS idx_emprestimo_data_emprestimo ON emprestimo(data_emprestimo);
CREATE INDEX IF NOT EXISTS idx_emprestimo_prazo_devolucao ON emprestimo(prazo_devolucao);
CREATE INDEX IF NOT EXISTS idx_emprestimo_data_devolucao ON emprestimo(data_devolucao);
CREATE INDEX IF NOT EXISTS idx_emprestimo_usuario_emprestimo_id ON emprestimo(usuario_emprestimo_id);
CREATE INDEX IF NOT EXISTS idx_emprestimo_usuario_responsavel_id ON emprestimo(usuario_responsavel_id);

-- Índice composto para queries de status (atrasado/em andamento/finalizado)
-- Otimiza queries que filtram por data_devolucao IS NULL AND prazo_devolucao < CURRENT_DATE
CREATE INDEX IF NOT EXISTS idx_emprestimo_status_check ON emprestimo(data_devolucao, prazo_devolucao);

-- Índices para tabela ITEM
-- Suporta queries de busca e autocomplete
CREATE INDEX IF NOT EXISTS idx_item_nome ON item(nome);
CREATE INDEX IF NOT EXISTS idx_item_patrimonio ON item(patrimonio);
CREATE INDEX IF NOT EXISTS idx_item_saldo ON item(saldo);

-- Índices para tabela USUARIO
-- Otimiza autenticação e buscas
CREATE INDEX IF NOT EXISTS idx_usuario_username ON usuario(username);
CREATE INDEX IF NOT EXISTS idx_usuario_email ON usuario(email);
CREATE INDEX IF NOT EXISTS idx_usuario_documento ON usuario(documento);

-- Índices de Foreign Keys para joins
-- Emprestimo Items
CREATE INDEX IF NOT EXISTS idx_emprestimo_item_emprestimo_id ON emprestimo_item(emprestimo_id);
CREATE INDEX IF NOT EXISTS idx_emprestimo_item_item_id ON emprestimo_item(item_id);

-- Reserva Items
CREATE INDEX IF NOT EXISTS idx_reserva_item_reserva_id ON reserva_item(reserva_id);
CREATE INDEX IF NOT EXISTS idx_reserva_item_item_id ON reserva_item(item_id);

-- Saida Items
CREATE INDEX IF NOT EXISTS idx_saida_item_saida_id ON saida_item(saida_id);
CREATE INDEX IF NOT EXISTS idx_saida_item_item_id ON saida_item(item_id);

-- Compra Items
CREATE INDEX IF NOT EXISTS idx_compra_item_compra_id ON compra_item(compra_id);
CREATE INDEX IF NOT EXISTS idx_compra_item_item_id ON compra_item(item_id);

-- Solicitacao Items
CREATE INDEX IF NOT EXISTS idx_solicitacao_item_solicitacao_id ON solicitacao_item(solicitacao_id);
CREATE INDEX IF NOT EXISTS idx_solicitacao_item_item_id ON solicitacao_item(item_id);

-- Emprestimo Devolucao Items
CREATE INDEX IF NOT EXISTS idx_emprestimo_devolucao_item_emprestimo_id ON emprestimo_devolucao_item(emprestimo_id);
CREATE INDEX IF NOT EXISTS idx_emprestimo_devolucao_item_item_id ON emprestimo_devolucao_item(item_id);

-- Índices para tabelas de referência (Cidade, Estado, Pais)
-- Estas raramente mudam e são muito consultadas
CREATE INDEX IF NOT EXISTS idx_cidade_estado_id ON cidade(estado_id);
CREATE INDEX IF NOT EXISTS idx_cidade_nome ON cidade(nome);
CREATE INDEX IF NOT EXISTS idx_estado_pais_id ON estado(pais_id);
CREATE INDEX IF NOT EXISTS idx_estado_uf ON estado(uf);

-- Índices para tabela FORNECEDOR
CREATE INDEX IF NOT EXISTS idx_fornecedor_razao_social ON fornecedor(razao_social);
CREATE INDEX IF NOT EXISTS idx_fornecedor_nome_fantasia ON fornecedor(nome_fantasia);

-- Índices para tabela COMPRA (dashboard queries)
CREATE INDEX IF NOT EXISTS idx_compra_data_compra ON compra(data_compra);
CREATE INDEX IF NOT EXISTS idx_compra_fornecedor_id ON compra(fornecedor_id);

-- Índices para tabela SAIDA (dashboard queries)
CREATE INDEX IF NOT EXISTS idx_saida_data_saida ON saida(data_saida);

-- Índices para tabela RESERVA
CREATE INDEX IF NOT EXISTS idx_reserva_data_reserva ON reserva(data_reserva);
CREATE INDEX IF NOT EXISTS idx_reserva_data_retirada ON reserva(data_retirada);
