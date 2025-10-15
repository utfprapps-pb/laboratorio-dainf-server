-- INSERT PERMISSAO
INSERT INTO PERMISSAO (NOME) values('ROLE_ADMINISTRADOR');
INSERT INTO PERMISSAO (NOME) values('ROLE_LABORATORISTA');
INSERT INTO PERMISSAO (NOME) values('ROLE_PROFESSOR');
INSERT INTO PERMISSAO (NOME) values('ROLE_ALUNO');

-- INSERT USUARIO (TODOS AS SENHAS SÃO: 123)
-- Nota: email_verificado default é FALSE
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Administrador', 'utfprapps-pb', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'utfprapps-pb@utfpr.edu.br', '000000', '4632202593', TRUE);
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Gustavo Arcari', 'gustavoarcari', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'gustavoarcari@utfpr.edu.br', '000000', '4632202592', TRUE);
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Vinicius Pegorini', 'vinicius', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'vinicius@professores.utfpr.edu.br', '2721471', '46999711050', TRUE);

-- INSERT USUARIOS ADICIONAIS PARA TESTES (do import.sql antigo)
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Gustavo Henrique Zaffani', 'gustavo', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'gzaffani@alunos.utfpr.edu.br', '1234', '46999668855', TRUE);
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Thiago Zaffani', 'thiago', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'thiago@alunos.utfpr.edu.br', '2345', '46999668855', TRUE);
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'João da Silva', 'joao', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'joao@alunos.utfpr.edu.br', '4567', '46999668855', TRUE);
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Antenor de Souza', 'antenor', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'antenor@alunos.utfpr.edu.br', '4568', '46999668855', TRUE);
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Mainara Lorencena', 'nara', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'nara@alunos.utfpr.edu.br', '4569', '46999668855', TRUE);
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Raul Cardoso', 'raul', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'raul@alunos.utfpr.edu.br', '4570', '46999668855', TRUE);
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Joana de Oliveira', 'joana', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'joana@alunos.utfpr.edu.br', '4571', '46999668855', TRUE);
INSERT INTO USUARIO (ID, NOME, USERNAME, PASSWORD, EMAIL, DOCUMENTO, TELEFONE, EMAIL_VERIFICADO) VALUES (nextval('usuario_id_seq'), 'Fábio Favarim', 'favarim', '$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC', 'favarim@professores.utfpr.edu.br', '3457', '46999668855', TRUE);

-- INSERT PERMISSÕES DOS USUÁRIOS
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (1, 1); -- Administrador -> ADMIN
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (2, 1); -- Gustavo Arcari -> ADMIN
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (3, 1); -- Vinicius -> ADMIN
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (4, 4); -- Gustavo Zaffani -> ALUNO
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (5, 4); -- Thiago -> ALUNO
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (6, 2); -- João -> LABORATORISTA
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (7, 2); -- Antenor -> LABORATORISTA
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (8, 3); -- Mainara -> PROFESSOR
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (9, 4); -- Raul -> ALUNO
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (10, 2); -- Joana -> LABORATORISTA
INSERT INTO USUARIO_PERMISSOES(USUARIO_ID, PERMISSOES_ID) VALUES (11, 3); -- Favarim -> PROFESSOR

--INSERT GRUPO
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Materiais Permanentes');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Materiais de Consumo');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Periféricos');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Outros');

-- GRUPOS ADICIONAIS PARA TESTES (do import.sql antigo)
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Arduinos');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 1');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 2');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 3');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 4');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 5');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 6');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 7');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 8');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 9');
INSERT INTO GRUPO (ID, DESCRICAO) VALUES (nextval('grupo_id_seq'), 'Grupo 10');

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
