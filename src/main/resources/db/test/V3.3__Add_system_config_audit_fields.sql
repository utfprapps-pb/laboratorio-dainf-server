-- Adiciona campos de auditoria à tabela system_config (ambiente de teste)
-- H2 não suporta múltiplas colunas em um ALTER TABLE, então fazemos separadamente
ALTER TABLE system_config ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE system_config ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE system_config ADD COLUMN created_by VARCHAR(255) NOT NULL DEFAULT 'system';
ALTER TABLE system_config ADD COLUMN updated_by VARCHAR(255);

