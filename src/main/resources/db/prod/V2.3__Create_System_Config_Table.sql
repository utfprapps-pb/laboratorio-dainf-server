CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY,
    nada_consta_email VARCHAR(255) NOT NULL,
    CONSTRAINT single_config_row CHECK (id = 1)
);

-- Insere uma linha padrão, se necessário
INSERT INTO system_config (id, nada_consta_email)
SELECT 1, 'default@utfpr.edu.br'
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE id = 1);
