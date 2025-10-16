CREATE TABLE nada_consta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    send_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255),
    CONSTRAINT fk_nada_consta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);


