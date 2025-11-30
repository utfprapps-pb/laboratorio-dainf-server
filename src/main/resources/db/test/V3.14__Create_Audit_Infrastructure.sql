-- Migration V3.14 - Infraestrutura de Auditoria (Hibernate Envers)
-- Versao H2 para testes
--
-- OBJETIVO: Criar tabelas para auditoria completa de entidades usando Hibernate Envers.
-- Registra todas as operacoes (INSERT, UPDATE, DELETE) com informacoes de quem fez e quando.

-- =====================================================
-- TABELA DE REVISOES (METADADOS DE AUDITORIA)
-- =====================================================

CREATE TABLE revinfo (
    id BIGINT NOT NULL PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    usuario VARCHAR(100),
    ip VARCHAR(45)
);

CREATE SEQUENCE revinfo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE revinfo ALTER COLUMN id SET DEFAULT nextval('revinfo_id_seq');

-- =====================================================
-- TABELAS DE AUDITORIA - ENTIDADES CORE
-- =====================================================

-- Emprestimo
CREATE TABLE emprestimo_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    data_emprestimo DATE,
    data_emprestimo_mod BOOLEAN,
    prazo_devolucao DATE,
    prazo_devolucao_mod BOOLEAN,
    data_devolucao DATE,
    data_devolucao_mod BOOLEAN,
    observacao VARCHAR(255),
    observacao_mod BOOLEAN,
    usuario_responsavel_id BIGINT,
    usuario_responsavel_id_mod BOOLEAN,
    usuario_emprestimo_id BIGINT,
    usuario_emprestimo_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_emprestimo_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- Item
CREATE TABLE item_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    nome VARCHAR(50),
    nome_mod BOOLEAN,
    patrimonio NUMERIC(38),
    patrimonio_mod BOOLEAN,
    siorg NUMERIC(38),
    siorg_mod BOOLEAN,
    valor NUMERIC(19,2),
    valor_mod BOOLEAN,
    qtde_minima NUMERIC(38,2),
    qtde_minima_mod BOOLEAN,
    localizacao VARCHAR(255),
    localizacao_mod BOOLEAN,
    tipo_item VARCHAR(1),
    tipo_item_mod BOOLEAN,
    saldo NUMERIC(38,2),
    saldo_mod BOOLEAN,
    grupo_id BIGINT,
    grupo_id_mod BOOLEAN,
    descricao VARCHAR(4000),
    descricao_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_item_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- Usuario (password e permissoes nao sao auditados)
CREATE TABLE usuario_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    nome VARCHAR(255),
    nome_mod BOOLEAN,
    username VARCHAR(100),
    username_mod BOOLEAN,
    documento VARCHAR(25),
    documento_mod BOOLEAN,
    email VARCHAR(100),
    email_mod BOOLEAN,
    telefone VARCHAR(15),
    telefone_mod BOOLEAN,
    foto_url VARCHAR(2048),
    foto_url_mod BOOLEAN,
    codigo_verificacao VARCHAR(512),
    codigo_verificacao_mod BOOLEAN,
    email_verificado BOOLEAN,
    email_verificado_mod BOOLEAN,
    ativo BOOLEAN,
    ativo_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_usuario_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- Saida
CREATE TABLE saida_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    data_saida DATE,
    data_saida_mod BOOLEAN,
    observacao VARCHAR(255),
    observacao_mod BOOLEAN,
    usuario_id BIGINT,
    usuario_id_mod BOOLEAN,
    emprestimo_id BIGINT,
    emprestimo_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_saida_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- Reserva
CREATE TABLE reserva_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    descricao VARCHAR(255),
    descricao_mod BOOLEAN,
    data_reserva DATE,
    data_reserva_mod BOOLEAN,
    data_retirada DATE,
    data_retirada_mod BOOLEAN,
    observacao VARCHAR(255),
    observacao_mod BOOLEAN,
    usuario_id BIGINT,
    usuario_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_reserva_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- Compra
CREATE TABLE compra_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    data_compra DATE,
    data_compra_mod BOOLEAN,
    fornecedor_id BIGINT,
    fornecedor_id_mod BOOLEAN,
    usuario_id BIGINT,
    usuario_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_compra_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- Solicitacao
CREATE TABLE solicitacao_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    descricao VARCHAR(255),
    descricao_mod BOOLEAN,
    data_solicitacao DATE,
    data_solicitacao_mod BOOLEAN,
    observacao VARCHAR(255),
    observacao_mod BOOLEAN,
    usuario_id BIGINT,
    usuario_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_solicitacao_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- Grupo
CREATE TABLE grupo_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    descricao VARCHAR(50),
    descricao_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_grupo_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- Fornecedor (cidade e estado nao sao auditados)
CREATE TABLE fornecedor_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    razao_social VARCHAR(80),
    razao_social_mod BOOLEAN,
    nome_fantasia VARCHAR(80),
    nome_fantasia_mod BOOLEAN,
    cnpj VARCHAR(14),
    cnpj_mod BOOLEAN,
    ie VARCHAR(14),
    ie_mod BOOLEAN,
    endereco VARCHAR(100),
    endereco_mod BOOLEAN,
    observacao VARCHAR(2000),
    observacao_mod BOOLEAN,
    email VARCHAR(255),
    email_mod BOOLEAN,
    telefone VARCHAR(15),
    telefone_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_fornecedor_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- NadaConsta
CREATE TABLE nada_consta_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    usuario_id BIGINT,
    usuario_id_mod BOOLEAN,
    status VARCHAR(20),
    status_mod BOOLEAN,
    send_at TIMESTAMP,
    send_at_mod BOOLEAN,
    created_at TIMESTAMP,
    created_at_mod BOOLEAN,
    updated_at TIMESTAMP,
    updated_at_mod BOOLEAN,
    created_by VARCHAR(255),
    created_by_mod BOOLEAN,
    updated_by VARCHAR(255),
    updated_by_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_nada_consta_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- =====================================================
-- TABELAS DE AUDITORIA - ENTIDADES JOIN
-- =====================================================

-- EmprestimoItem
CREATE TABLE emprestimo_item_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    qtde NUMERIC(38,2),
    qtde_mod BOOLEAN,
    item_id BIGINT,
    item_id_mod BOOLEAN,
    emprestimo_id BIGINT,
    emprestimo_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_emprestimo_item_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- EmprestimoDevolucaoItem
CREATE TABLE emprestimo_devolucao_item_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    qtde NUMERIC(38,2),
    qtde_mod BOOLEAN,
    status VARCHAR(1),
    status_mod BOOLEAN,
    item_id BIGINT,
    item_id_mod BOOLEAN,
    emprestimo_id BIGINT,
    emprestimo_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_emprestimo_devolucao_item_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- SaidaItem
CREATE TABLE saida_item_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    qtde NUMERIC(38,2),
    qtde_mod BOOLEAN,
    item_id BIGINT,
    item_id_mod BOOLEAN,
    saida_id BIGINT,
    saida_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_saida_item_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- ReservaItem
CREATE TABLE reserva_item_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    qtde NUMERIC(38,2),
    qtde_mod BOOLEAN,
    item_id BIGINT,
    item_id_mod BOOLEAN,
    reserva_id BIGINT,
    reserva_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_reserva_item_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- CompraItem
CREATE TABLE compra_item_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    qtde NUMERIC(38,2),
    qtde_mod BOOLEAN,
    valor NUMERIC(19,2),
    valor_mod BOOLEAN,
    item_id BIGINT,
    item_id_mod BOOLEAN,
    compra_id BIGINT,
    compra_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_compra_item_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- SolicitacaoItem
CREATE TABLE solicitacao_item_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    qtde NUMERIC(38,2),
    qtde_mod BOOLEAN,
    item_id BIGINT,
    item_id_mod BOOLEAN,
    solicitacao_id BIGINT,
    solicitacao_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_solicitacao_item_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);

-- ItemImage
CREATE TABLE item_image_aud (
    id BIGINT NOT NULL,
    rev BIGINT NOT NULL,
    revtype SMALLINT,
    content_type VARCHAR(255),
    content_type_mod BOOLEAN,
    name_image VARCHAR(255),
    name_image_mod BOOLEAN,
    item_id BIGINT,
    item_id_mod BOOLEAN,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_item_image_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(id)
);
