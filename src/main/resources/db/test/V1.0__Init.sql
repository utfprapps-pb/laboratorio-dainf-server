CREATE TABLE master_pais
(
    id    bigint,
    nome  varchar(30) not null,
    sigla varchar(2)  not null,
    PRIMARY KEY (id)
);

CREATE SEQUENCE master_pais_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;

CREATE TABLE master_estado
(
    id             bigint,
    master_pais_id bigint,
    nome           varchar(30) not null,
    sigla          varchar(2)  not null,
    PRIMARY KEY (id),
    CONSTRAINT master_estado_master_pais_id_fk FOREIGN KEY (master_pais_id)
        REFERENCES master_pais (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE SEQUENCE master_estado_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;

CREATE TABLE master_cidade
(
    id               bigint,
    master_estado_id bigint,
    nome             varchar(560) not null,
    PRIMARY KEY (id),
    CONSTRAINT master_cidade_master_estado_id_fk FOREIGN KEY (master_estado_id)
        REFERENCES master_estado (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE master_plano
(
    id             bigint  NOT NULL,
    titulo         character varying(30),
    descricao      character varying(500),
    valor_plano    numeric(10, 2),
    observacao     character varying(1000),
    ativo          boolean NOT NULL,
    paypal_plan_id character varying(100),
    licencas       bigint  NOT NULL,
    CONSTRAINT master_plano_pkey PRIMARY KEY (id)
);


CREATE TABLE master_plano_detalhe
(
    id              bigint NOT NULL,
    descricao       character varying(80),
    master_plano_id bigint,
    CONSTRAINT master_plano_detalhe_pkey PRIMARY KEY (id),
    CONSTRAINT master_plano_detalhe_master_plano_id_fk FOREIGN KEY (master_plano_id)
        REFERENCES master_plano (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE SEQUENCE master_tenant_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;

CREATE TABLE master_tenant
(
    id                      bigint  NOT NULL DEFAULT nextval('master_tenant_seq'::regclass),
    password                character varying(30),
    tenant_id               character varying(30),
    url                     character varying(256),
    username                character varying(30),
    version                 integer NOT NULL,
    subscribe_id            character varying(150),
    master_plano_id         bigint,
    nome_razao              character varying(100),
    cpf_cnpj                character varying(18),
    rg_ie                   character varying(15),
    celular                 character varying(20),
    telefone                character varying(25),
    email                   character varying(50),
    quantidade_licencas     integer          default 0,
    logo_nome               character varying(100),
    logo_arquivo            oid,
    data_inicio_subscribe   date,
    data_atualizao_status   date,
    subscribe_status        character varying(50),
    ativo                   boolean          default true,
    trial                   boolean          default true,
    bairro                  character varying(50),
    cep                     character varying(15),
    complemento             character varying(100),
    numero                  character varying(9),
    rua                     character varying(50),
    nome_exibicao_relatorio character varying(100),
    master_cidade_id        bigint,
    token_confirmacao       character varying(255),
    registro_confirmado     boolean NOT NULL default false,
    CONSTRAINT master_tenant_pkey PRIMARY KEY (id)
);

CREATE TABLE master_tabela_indice
(
    id               bigint                      NOT NULL,
    ano              integer                     NOT NULL,
    data_modificacao timestamp without time zone NOT NULL,
    data_cadastro    date                        NOT NULL,
    mes              integer                     NOT NULL,
    valor            numeric(15, 2)              NOT NULL,
    master_indice_id bigint,
    CONSTRAINT master_tabela_indice_pk PRIMARY KEY (id),
    CONSTRAINT unqmesanoindiceid UNIQUE (mes, ano, master_indice_id)
);

CREATE SEQUENCE master_cidade_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;

CREATE SEQUENCE master_error_log_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
ALTER TABLE master_error_log_seq
    OWNER TO postgres;

CREATE SEQUENCE master_plano_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
ALTER TABLE master_plano_seq
    OWNER TO postgres;


CREATE SEQUENCE master_plano_detalhe_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
ALTER TABLE master_plano_detalhe_seq
    OWNER TO postgres;



ALTER TABLE master_tenant
    ADD CONSTRAINT master_tenant_master_plano_id_fk FOREIGN KEY (master_plano_id) REFERENCES master_plano (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE master_tenant
    ADD CONSTRAINT master_tenant_cidade_id_fk FOREIGN KEY (master_cidade_id) REFERENCES master_cidade (id) ON UPDATE NO ACTION ON DELETE NO ACTION;



--- **************************************************************
--- CREATE TENANT DB TRIGGER
--- **************************************************************
CREATE EXTENSION dblink; -- install the extension dblink to be able to perform create databe, because postgres don't allow to execute a create a database inside a transaction.

-- function to create a database
CREATE OR REPLACE FUNCTION new_tenant_function()
    RETURNS trigger AS
$BODY$
DECLARE
    db_name varchar;
BEGIN
    db_name = concat('eightc_', new.id);
    IF EXISTS(SELECT 1 FROM pg_database WHERE datname = db_name) THEN -- verify if the database exists
        RAISE NOTICE 'Database already exists';
    ELSE
        PERFORM dblink_connect('conn', 'host=127.0.0.1 port=5432 dbname=postgres user=postgres password=' ||
                                       quote_ident(new.password)); -- perform the connection
        PERFORM dblink_exec('conn', 'CREATE DATABASE ' || quote_ident(db_name)); -- create the database
        PERFORM dblink_disconnect('conn');
    END IF;
    RETURN NEW;
END;
$BODY$
    LANGUAGE plpgsql
    VOLATILE -- Says the function is implemented in the plpgsql language; VOLATILE says the function has side effects.
    COST 100; -- Estimated execution cost of the function.

-- trigger to call a function to create the database
CREATE TRIGGER new_tenant_trigger
    AFTER INSERT
    ON master_tenant
    FOR EACH ROW
EXECUTE PROCEDURE new_tenant_function();
--- **************************************************************
--- FIM CREATE TENANT DB TRIGGER
--- **************************************************************


--- **************************************************************
--- 					PLANS PAYPAL TEST
--- ***************************************************************

--- INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, paypal_plan_id, licencas)
--- VALUES (1, 'Personal', 'Para indivíduos e equipes pequenas. Você pode atualizar o plano quando quiser!', 39.90, '',
---         true, 'P-25V23996MU8760215LYQ7QUY', 1);
--- INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, paypal_plan_id, licencas)
--- VALUES (2, 'Business',
---         'Para empresas que desejam permanecer competitivas no mercado e crescer com a ajuda da tecnologia!', 59.90, '',
---         true, 'P-1WX24143F39760906LYQ7RKQ', 3);
--- INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, paypal_plan_id, licencas)
--- VALUES (3, 'Enterprise',
---         'Tudo o que você pode obter de uma solução profissional para manter seus negócios a caminho do sucesso!', 99.90,
---         '', true, 'P-6N8460311T524783SLYQ7RVI', 10);
        
--- **************************************************************

--- **************************************************************
--- 					PLANS PAYPAL PRODUCTION
--- ***************************************************************
 
INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, paypal_plan_id, licencas)
VALUES (1, 'Personal', 'Para indivíduos e equipes pequenas. Você pode atualizar o plano quando quiser!', 39.90, '',
        true, 'P-2CK60235N34033948L4CQBSA', 1);
INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, paypal_plan_id, licencas)
VALUES (2, 'Business',
        'Para empresas que desejam permanecer competitivas no mercado e crescer com a ajuda da tecnologia!', 59.90, '',
        true, 'P-6XV659425E5764640L4CQB5I', 3);
INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, paypal_plan_id, licencas)
VALUES (3, 'Enterprise',
        'Tudo o que você pode obter de uma solução profissional para manter seus negócios a caminho do sucesso!', 99.90,
        '', true, 'P-89T321951U497023CL4CQCHI', 10);
INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, licencas)
VALUES (4, 'Teste', 'Para teste da ferramenta, com possibilidade de apenas 1 (um) cálculo!', 00.00, '', true, 1);
INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, licencas)
VALUES (5, 'Personalizado 15', 'Plano personalizado com 15 licenças de uso!', 00.00, '', false, 15);
INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, licencas)
VALUES (6, 'Personalizado 20', 'Plano personalizado com 20 licenças de uso!', 00.00, '', false, 20);
INSERT INTO master_plano(id, titulo, descricao, valor_plano, observacao, ativo, licencas)
VALUES (7, 'Personalizado 30', 'Plano personalizado com 30 licenças de uso!', 00.00, '', false, 30);

INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (1, 'Cálculos ilimitados', 1);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (2, 'Cálculos de juros moratórios', 1);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (3, 'Cálculos de juros compensatórios', 1);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (4, 'Cálculo de multa', 1);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (5, '1 usuário', 1);

INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (6, 'Cálculos ilimitados', 2);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (7, 'Cálculos de juros moratórios', 2);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (8, 'Cálculos de juros compensatórios', 2);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (9, 'Cálculo de multa', 2);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (10, '3 usuários', 2);

INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (11, 'Cálculos ilimitados', 3);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (12, 'Cálculos de juros moratórios', 3);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (13, 'Cálculos de juros compensatórios', 3);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (14, 'Cálculo de multa', 3);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (15, '10 Usuários', 3);

INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (16, 'Apenas 1 (um) cálculo', 4);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (17, 'Cálculos de juros moratórios', 4);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (18, 'Cálculos de juros compensatórios', 4);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (19, 'Cálculo de multa', 4);
INSERT INTO master_plano_detalhe(id, descricao, master_plano_id)
VALUES (20, '1 Usuário teste.', 4);

CREATE TABLE master_error_log
(
    id               bigint                      NOT NULL,
    erro             text,
    data_cadastro    timestamp without time zone NOT NULL,
    data_modificacao timestamp without time zone NOT NULL,
    master_tenant_id bigint,
    CONSTRAINT fk_master_error_log_master_tenant_id FOREIGN KEY (master_tenant_id)
        REFERENCES master_tenant (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT master_error_log_pkey PRIMARY KEY (id)
);

CREATE TABLE master_indice
(
    id    bigint  NOT NULL,
    ativo boolean NOT NULL,
    nome  character varying(255),
    CONSTRAINT master_indice_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE master_indice_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
ALTER TABLE master_indice_seq
    OWNER TO postgres;


CREATE SEQUENCE master_tabela_indice_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
ALTER TABLE master_tabela_indice_seq
    OWNER TO postgres;

CREATE SEQUENCE master_motivo_cancelamento_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
ALTER TABLE master_motivo_cancelamento_seq
    OWNER TO postgres;

CREATE TABLE master_motivo_cancelamento
(
    id        bigint NOT NULL,
    descricao character varying(80),
    CONSTRAINT master_motivo_cancelamento_pk PRIMARY KEY (id)
);

CREATE SEQUENCE master_cancelamento_seq
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
ALTER TABLE master_cancelamento_seq
    OWNER TO postgres;

CREATE TABLE master_cancelamento
(
    id                bigint NOT NULL,
    master_motivo_id  bigint,
    observacao        character varying(1000),
    master_tenant_id  bigint,
    data_cancelamento date,
    CONSTRAINT master_cancelamento_pk PRIMARY KEY (id),
    CONSTRAINT fk_master_cancelamento_motivo FOREIGN KEY (master_motivo_id)
        REFERENCES master_motivo_cancelamento (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fk_master_cancelamento_master_tenant FOREIGN KEY (master_tenant_id)
        REFERENCES master_tenant (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE NO ACTION
);
