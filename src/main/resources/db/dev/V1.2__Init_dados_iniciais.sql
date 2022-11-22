-- INSERT PERMISSAO
INSERT INTO PERMISSAO (ID, NOME) values(nextval('permissao_id_seq'), 'ROLE_ADMINISTRADOR');
INSERT INTO PERMISSAO (ID, NOME) values(nextval('permissao_id_seq'), 'ROLE_LABORATORISTA');
INSERT INTO PERMISSAO (ID, NOME) values(nextval('permissao_id_seq'), 'ROLE_PROFESSOR');
INSERT INTO PERMISSAO (ID, NOME) values(nextval('permissao_id_seq'), 'ROLE_ALUNO');

-- INSERT USUARIO (TODOS AS SENHAS SÃO: 123)
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE) VALUES (nextval('usuario_id_seq'), 'Administrador', 'utfprapps-pb', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'utfprapps-pb@utfpr.edu.br', '000000', '4632202593');
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE) VALUES (nextval('usuario_id_seq'), 'Gustavo Arcari', 'gustavoarcari', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'gustavoarcari@utfpr.edu.br', '000000', '4632202592');
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE) VALUES (nextval('usuario_id_seq'), 'Vinicius Pegorini', 'vinicius', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'vinicius@professores.utfpr.edu.br', '2721471', '46999711050');

-- INSERT PERMISSÕES DOS USUÁRIOS
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (1, 1);
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (2, 1);
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (3, 1);

--INSERT GRUPO
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Materiais Permanentes');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Materiais de Consumo');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Periféricos');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Outros');

-- INSERT FORNECEDOR
INSERT INTO FORNECEDOR (ID, RAZAO_SOCIAL, NOME_FANTASIA, CNPJ, IE, ENDERECO, CIDADE_ID, ESTADO_ID) VALUES (nextval('fornecedor_id_seq'), 'Fornecedor Padrão', 'Nome Fornecedor Padrão', '75101873000190', '1239987', 'Endereço do Fornecedor Padrão', 1, 2);

--INSERT ITEM
INSERT INTO ITEM (ID, NOME, PATRIMONIO, SIORG, VALOR, QTDE_MINIMA, LOCALIZACAO, TIPO_ITEM, SALDO, GRUPO_ID) VALUES (nextval('item_id_seq'), '(Novo Item)', 0000, 0000, 000.00, 1, 'Não editar esse item!', 'C', 1, 1);

-- INSERT RELATORIO
INSERT INTO RELATORIO (ID, NAME_REPORT, NOME) VALUES (nextval('relatorio_id_seq'), 'HistoricoEmprestimoUsuario.jrxml', 'Histórico de Empréstimo do Usuário X');
INSERT INTO RELATORIO (ID, NAME_REPORT, NOME) VALUES (nextval('relatorio_id_seq'), 'ItensSemEstoque.jrxml', 'Itens Sem Saldo no Estoque');
INSERT INTO RELATORIO (ID, NAME_REPORT, NOME) VALUES (nextval('relatorio_id_seq'), 'EmprestimosRealizadosEntre.jrxml', 'Emprestéstimos Realizados Entre');
INSERT INTO RELATORIO (ID, NAME_REPORT, NOME) VALUES (nextval('relatorio_id_seq'), 'ReservaDoItem.jrxml', 'Reservas do Item');
INSERT INTO RELATORIO (ID, NAME_REPORT, NOME) VALUES (nextval('relatorio_id_seq'), 'SolicitacaoItem.jrxml', 'Solicitações de Compra do Item');
INSERT INTO RELATORIO (ID, NAME_REPORT, NOME) VALUES (nextval('relatorio_id_seq'), 'ItensAtingiramQtdeMinima.jrxml', 'Itens que Atingiram a Quantidade Mínima');

-- INSERT RELATORIO_PARAM
INSERT INTO RELATORIO_PARAMS (ID, ALIAS_PARAM, NAME_PARAM, TIPO_PARAM, RELATORIO_ID) VALUES (nextval('relatorio_params_id_seq'), 'RA/ SIAPE', 'DOCUMENTO', 'S', 1);
INSERT INTO RELATORIO_PARAMS (ID, ALIAS_PARAM, NAME_PARAM, TIPO_PARAM, RELATORIO_ID) VALUES (nextval('relatorio_params_id_seq'), 'Data Inicial', 'DT_INI', 'D', 3);
INSERT INTO RELATORIO_PARAMS (ID, ALIAS_PARAM, NAME_PARAM, TIPO_PARAM, RELATORIO_ID) VALUES (nextval('relatorio_params_id_seq'), 'Data Final', 'DT_FIM', 'D', 3);
INSERT INTO RELATORIO_PARAMS (ID, ALIAS_PARAM, NAME_PARAM, TIPO_PARAM, RELATORIO_ID) VALUES (nextval('relatorio_params_id_seq'), 'Cód do Item', 'ID_ITEM', 'N', 4);
INSERT INTO RELATORIO_PARAMS (ID, ALIAS_PARAM, NAME_PARAM, TIPO_PARAM, RELATORIO_ID) VALUES (nextval('relatorio_params_id_seq'), 'Cód do Item', 'ID_ITEM', 'N', 5)
