-- Fix email_verificado column: add default value and NOT NULL constraint
-- H2 compatible version for test environment

-- Update existing NULL values to FALSE (for any rows created before this migration)
UPDATE usuario SET email_verificado = FALSE WHERE email_verificado IS NULL;

-- Add NOT NULL constraint and default value
ALTER TABLE usuario ALTER COLUMN email_verificado SET DEFAULT FALSE;
ALTER TABLE usuario ALTER COLUMN email_verificado SET NOT NULL;