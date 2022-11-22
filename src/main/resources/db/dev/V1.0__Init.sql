CREATE TABLE cidade (
    id bigint NOT NULL,
    nome character varying(60) NOT NULL,
    estado_id bigint
);
CREATE SEQUENCE cidade_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE cidade_id_seq OWNED BY cidade.id;

CREATE TABLE compra (
                               id bigint NOT NULL,
                               data_compra date NOT NULL,
                               fornecedor_id bigint NOT NULL,
                               usuario_id bigint
);
CREATE SEQUENCE compra_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE compra_id_seq OWNED BY compra.id;


CREATE TABLE compra_item (
                                    id bigint NOT NULL,
                                    qtde numeric(19,2) NOT NULL,
                                    valor numeric(19,2) NOT NULL,
                                    compra_id bigint,
                                    item_id bigint NOT NULL
);
CREATE SEQUENCE compra_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE compra_item_id_seq OWNED BY compra_item.id;

CREATE TABLE emprestimo (
                                   id bigint NOT NULL,
                                   data_devolucao date,
                                   data_emprestimo date NOT NULL,
                                   observacao character varying(255),
                                   prazo_devolucao date,
                                   usuario_emprestimo_id bigint,
                                   usuario_responsavel_id bigint
);
CREATE SEQUENCE emprestimo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE emprestimo_id_seq OWNED BY emprestimo.id;

CREATE TABLE emprestimo_devolucao_item (
                                                  id bigint NOT NULL,
                                                  qtde numeric(19,2) NOT NULL,
                                                  status character varying(1),
                                                  emprestimo_id bigint,
                                                  item_id bigint NOT NULL
);
CREATE SEQUENCE emprestimo_devolucao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE emprestimo_devolucao_item_id_seq OWNED BY emprestimo_devolucao_item.id;

CREATE TABLE emprestimo_item (
                                        id bigint NOT NULL,
                                        qtde numeric(19,2) NOT NULL,
                                        emprestimo_id bigint,
                                        item_id bigint NOT NULL
);
CREATE SEQUENCE emprestimo_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE emprestimo_item_id_seq OWNED BY emprestimo_item.id;

CREATE TABLE estado (
                               id bigint NOT NULL,
                               nome character varying(255),
                               uf character varying(2),
                               pais_id bigint
);
CREATE SEQUENCE estado_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE estado_id_seq OWNED BY estado.id;

CREATE TABLE fornecedor (
                                   id bigint NOT NULL,
                                   cnpj character varying(14) NOT NULL,
                                   email character varying(255),
                                   endereco character varying(100),
                                   ie character varying(14) NOT NULL,
                                   nome_fantasia character varying(80) NOT NULL,
                                   observacao character varying(2000),
                                   razao_social character varying(80) NOT NULL,
                                   telefone character varying(15),
                                   cidade_id bigint NOT NULL,
                                   estado_id bigint NOT NULL
);
CREATE SEQUENCE fornecedor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE fornecedor_id_seq OWNED BY fornecedor.id;

CREATE TABLE grupo (
                              id bigint NOT NULL,
                              descricao character varying(50) NOT NULL
);
CREATE SEQUENCE grupo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE grupo_id_seq OWNED BY grupo.id;

CREATE TABLE item (
                             id bigint NOT NULL,
                             descricao character varying(4000),
                             localizacao character varying(255),
                             nome character varying(50) NOT NULL,
                             patrimonio numeric(19,2),
                             qtde_minima numeric(19,2) NOT NULL,
                             saldo numeric(19,2),
                             siorg numeric(19,2),
                             tipo_item character varying(1),
                             valor numeric(19,2) DEFAULT 0.00,
                             grupo_id bigint
);
CREATE SEQUENCE item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE item_id_seq OWNED BY item.id;

CREATE TABLE item_image (
                                   id bigint NOT NULL,
                                   caminho_image character varying(255),
                                   name_image character varying(255),
                                   item_id bigint
);
CREATE SEQUENCE item_image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE item_image_id_seq OWNED BY item_image.id;

CREATE TABLE pais (
                             id bigint NOT NULL,
                             nome character varying(50) NOT NULL,
                             sigla character varying(3) NOT NULL
);
CREATE SEQUENCE pais_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE pais_id_seq OWNED BY pais.id;

CREATE TABLE permissao (
                                  id bigint NOT NULL,
                                  nome character varying(20) NOT NULL
);

CREATE SEQUENCE permissao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE permissao_id_seq OWNED BY permissao.id;

CREATE TABLE relatorio (
                                  id bigint NOT NULL,
                                  name_report character varying(255),
                                  nome character varying(255) NOT NULL
);
CREATE SEQUENCE relatorio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE relatorio_id_seq OWNED BY relatorio.id;

CREATE TABLE relatorio_params (
                                         id bigint NOT NULL,
                                         alias_param character varying(50) NOT NULL,
                                         name_param character varying(30) NOT NULL,
                                         tipo_param character varying(1) NOT NULL,
                                         relatorio_id bigint
);
CREATE SEQUENCE relatorio_params_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE relatorio_params_id_seq OWNED BY relatorio_params.id;

CREATE TABLE reserva (
                                id bigint NOT NULL,
                                data_reserva date,
                                data_retirada date,
                                descricao character varying(255),
                                observacao character varying(255),
                                usuario_id bigint
);
CREATE SEQUENCE reserva_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE reserva_id_seq OWNED BY reserva.id;

CREATE TABLE reserva_item (
                                     id bigint NOT NULL,
                                     qtde numeric(19,2) NOT NULL,
                                     item_id bigint,
                                     reserva_id bigint,
                                     CONSTRAINT reserva_item_qtde_check CHECK ((qtde >= (1)::numeric))
);
CREATE SEQUENCE reserva_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE reserva_item_id_seq OWNED BY reserva_item.id;

CREATE TABLE saida (
                              id bigint NOT NULL,
                              data_saida date,
                              emprestimo_id bigint,
                              observacao character varying(255),
                              usuario_id bigint
);
CREATE SEQUENCE saida_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE saida_id_seq OWNED BY saida.id;

CREATE TABLE saida_item (
                                   id bigint NOT NULL,
                                   qtde numeric(19,2) NOT NULL,
                                   item_id bigint NOT NULL,
                                   saida_id bigint
);
CREATE SEQUENCE saida_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE saida_item_id_seq OWNED BY saida_item.id;

CREATE TABLE solicitacao (
                                    id bigint NOT NULL,
                                    data_solicitacao date NOT NULL,
                                    descricao character varying(255) NOT NULL,
                                    observacao character varying(255),
                                    usuario_id bigint
);
CREATE SEQUENCE solicitacao_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE solicitacao_id_seq OWNED BY solicitacao.id;

CREATE TABLE solicitacao_item (
                                         id bigint NOT NULL,
                                         qtde numeric(19,2) NOT NULL,
                                         item_id bigint,
                                         solicitacao_id bigint,
                                         CONSTRAINT solicitacao_item_qtde_check CHECK ((qtde >= (1)::numeric))
);
CREATE SEQUENCE solicitacao_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE solicitacao_item_id_seq OWNED BY solicitacao_item.id;

CREATE TABLE usuario (
                                id bigint NOT NULL,
                                documento character varying(25),
                                email character varying(100) NOT NULL,
                                nome character varying(255) NOT NULL,
                                password character varying(255) NOT NULL,
                                telefone character varying(15) NOT NULL,
                                username character varying(100) NOT NULL
);
CREATE SEQUENCE usuario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE usuario_id_seq OWNED BY usuario.id;

CREATE TABLE usuario_permissoes (
                                           usuario_id bigint NOT NULL,
                                           permissoes_id bigint NOT NULL
);
ALTER TABLE ONLY cidade ALTER COLUMN id SET DEFAULT nextval('cidade_id_seq'::regclass);
ALTER TABLE ONLY compra ALTER COLUMN id SET DEFAULT nextval('compra_id_seq'::regclass);
ALTER TABLE ONLY compra_item ALTER COLUMN id SET DEFAULT nextval('compra_item_id_seq'::regclass);
ALTER TABLE ONLY emprestimo ALTER COLUMN id SET DEFAULT nextval('emprestimo_id_seq'::regclass);
ALTER TABLE ONLY emprestimo_devolucao_item ALTER COLUMN id SET DEFAULT nextval('emprestimo_devolucao_item_id_seq'::regclass);
ALTER TABLE ONLY emprestimo_item ALTER COLUMN id SET DEFAULT nextval('emprestimo_item_id_seq'::regclass);
ALTER TABLE ONLY estado ALTER COLUMN id SET DEFAULT nextval('estado_id_seq'::regclass);
ALTER TABLE ONLY fornecedor ALTER COLUMN id SET DEFAULT nextval('fornecedor_id_seq'::regclass);
ALTER TABLE ONLY grupo ALTER COLUMN id SET DEFAULT nextval('grupo_id_seq'::regclass);
ALTER TABLE ONLY item ALTER COLUMN id SET DEFAULT nextval('item_id_seq'::regclass);
ALTER TABLE ONLY item_image ALTER COLUMN id SET DEFAULT nextval('item_image_id_seq'::regclass);
ALTER TABLE ONLY pais ALTER COLUMN id SET DEFAULT nextval('pais_id_seq'::regclass);
ALTER TABLE ONLY permissao ALTER COLUMN id SET DEFAULT nextval('permissao_id_seq'::regclass);
ALTER TABLE ONLY relatorio ALTER COLUMN id SET DEFAULT nextval('relatorio_id_seq'::regclass);
ALTER TABLE ONLY relatorio_params ALTER COLUMN id SET DEFAULT nextval('relatorio_params_id_seq'::regclass);
ALTER TABLE ONLY reserva ALTER COLUMN id SET DEFAULT nextval('reserva_id_seq'::regclass);
ALTER TABLE ONLY reserva_item ALTER COLUMN id SET DEFAULT nextval('reserva_item_id_seq'::regclass);
ALTER TABLE ONLY saida ALTER COLUMN id SET DEFAULT nextval('saida_id_seq'::regclass);
ALTER TABLE ONLY saida_item ALTER COLUMN id SET DEFAULT nextval('saida_item_id_seq'::regclass);
ALTER TABLE ONLY solicitacao ALTER COLUMN id SET DEFAULT nextval('solicitacao_id_seq'::regclass);
ALTER TABLE ONLY solicitacao_item ALTER COLUMN id SET DEFAULT nextval('solicitacao_item_id_seq'::regclass);
ALTER TABLE ONLY usuario ALTER COLUMN id SET DEFAULT nextval('usuario_id_seq'::regclass);

ALTER TABLE ONLY cidade
    ADD CONSTRAINT cidade_pkey PRIMARY KEY (id);
ALTER TABLE ONLY compra_item
    ADD CONSTRAINT compra_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY compra
    ADD CONSTRAINT compra_pkey PRIMARY KEY (id);
ALTER TABLE ONLY emprestimo_devolucao_item
    ADD CONSTRAINT emprestimo_devolucao_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY emprestimo_item
    ADD CONSTRAINT emprestimo_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY emprestimo
    ADD CONSTRAINT emprestimo_pkey PRIMARY KEY (id);
ALTER TABLE ONLY estado
    ADD CONSTRAINT estado_pkey PRIMARY KEY (id);
ALTER TABLE ONLY fornecedor
    ADD CONSTRAINT fornecedor_pkey PRIMARY KEY (id);
ALTER TABLE ONLY grupo
    ADD CONSTRAINT grupo_pkey PRIMARY KEY (id);
ALTER TABLE ONLY item_image
    ADD CONSTRAINT item_image_pkey PRIMARY KEY (id);
ALTER TABLE ONLY item
    ADD CONSTRAINT item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY pais
    ADD CONSTRAINT pais_pkey PRIMARY KEY (id);
ALTER TABLE ONLY permissao
    ADD CONSTRAINT permissao_pkey PRIMARY KEY (id);
ALTER TABLE ONLY relatorio_params
    ADD CONSTRAINT relatorio_params_pkey PRIMARY KEY (id);
ALTER TABLE ONLY relatorio
    ADD CONSTRAINT relatorio_pkey PRIMARY KEY (id);
ALTER TABLE ONLY reserva_item
    ADD CONSTRAINT reserva_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY reserva
    ADD CONSTRAINT reserva_pkey PRIMARY KEY (id);
ALTER TABLE ONLY saida_item
    ADD CONSTRAINT saida_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY saida
    ADD CONSTRAINT saida_pkey PRIMARY KEY (id);
ALTER TABLE ONLY solicitacao_item
    ADD CONSTRAINT solicitacao_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY solicitacao
    ADD CONSTRAINT solicitacao_pkey PRIMARY KEY (id);
ALTER TABLE ONLY usuario
    ADD CONSTRAINT uk_username UNIQUE (username);
ALTER TABLE ONLY usuario_permissoes
    ADD CONSTRAINT usuario_permissoes_pkey PRIMARY KEY (usuario_id, permissoes_id);
ALTER TABLE ONLY usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);
ALTER TABLE ONLY reserva_item
    ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(id);
ALTER TABLE ONLY compra
    ADD CONSTRAINT fk_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor(id);
ALTER TABLE ONLY emprestimo
    ADD CONSTRAINT fk_usuario_responsavel FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario(id);
ALTER TABLE ONLY solicitacao_item
    ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(id);
ALTER TABLE ONLY solicitacao_item
    ADD CONSTRAINT fk_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES solicitacao(id);
ALTER TABLE ONLY saida_item
    ADD CONSTRAINT fk_saida FOREIGN KEY (saida_id) REFERENCES saida(id);
ALTER TABLE ONLY emprestimo_devolucao_item
    ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(id);
ALTER TABLE ONLY fornecedor
    ADD CONSTRAINT fk_estado FOREIGN KEY (estado_id) REFERENCES estado(id);
ALTER TABLE ONLY saida
    ADD CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
ALTER TABLE ONLY usuario_permissoes
    ADD CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
ALTER TABLE ONLY reserva_item
    ADD CONSTRAINT fk_reserva FOREIGN KEY (reserva_id) REFERENCES reserva(id);
ALTER TABLE ONLY emprestimo_devolucao_item
    ADD CONSTRAINT fk_emprestimo FOREIGN KEY (emprestimo_id) REFERENCES emprestimo(id);
ALTER TABLE ONLY solicitacao
    ADD CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
ALTER TABLE ONLY emprestimo_item
    ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(id);
ALTER TABLE ONLY emprestimo
    ADD CONSTRAINT fk_usuario FOREIGN KEY (usuario_emprestimo_id) REFERENCES usuario(id);
ALTER TABLE ONLY reserva
    ADD CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
ALTER TABLE ONLY item
    ADD CONSTRAINT fk_grupo FOREIGN KEY (grupo_id) REFERENCES grupo(id);
ALTER TABLE ONLY compra
    ADD CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
ALTER TABLE ONLY cidade
    ADD CONSTRAINT fk_estado FOREIGN KEY (estado_id) REFERENCES estado(id);
ALTER TABLE ONLY usuario_permissoes
    ADD CONSTRAINT fk_permissao FOREIGN KEY (permissoes_id) REFERENCES permissao(id);
ALTER TABLE ONLY compra_item
    ADD CONSTRAINT fk_compra FOREIGN KEY (compra_id) REFERENCES compra(id);
ALTER TABLE ONLY estado
    ADD CONSTRAINT fk_pais FOREIGN KEY (pais_id) REFERENCES pais(id);
ALTER TABLE ONLY fornecedor
    ADD CONSTRAINT fk_cidade FOREIGN KEY (cidade_id) REFERENCES cidade(id);
ALTER TABLE ONLY relatorio_params
    ADD CONSTRAINT fk_relatorio FOREIGN KEY (relatorio_id) REFERENCES relatorio(id);
ALTER TABLE ONLY emprestimo_item
    ADD CONSTRAINT fk_emprestimo FOREIGN KEY (emprestimo_id) REFERENCES emprestimo(id);
ALTER TABLE ONLY compra_item
    ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(id);
ALTER TABLE ONLY item_image
    ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(id);
ALTER TABLE ONLY saida_item
    ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(id);
