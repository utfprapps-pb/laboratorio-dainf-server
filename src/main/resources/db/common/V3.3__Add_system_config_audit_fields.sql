-- Adiciona campos de auditoria à tabela system_config
ALTER TABLE system_config
  ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  ADD COLUMN updated_at TIMESTAMP,
  ADD COLUMN created_by VARCHAR(255) NOT NULL DEFAULT 'system',
  ADD COLUMN updated_by VARCHAR(255);

