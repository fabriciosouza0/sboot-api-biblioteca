-- V12: created_at com DEFAULT now() em todas as tabelas
-- 1) adms: adicionar created_at (tabela sem timestamp de auditoria)
-- 2) refresh_token: corrigir tipo timestamp → TIMESTAMPTZ e DEFAULT CURRENT_TIMESTAMP → now()

-- ==== adms: created_at ====
ALTER TABLE adms ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- ==== refresh_token: corrigir created_at ====
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'refresh_token' AND column_name = 'created_at'
        AND data_type = 'timestamp without time zone'
    ) THEN
        ALTER TABLE refresh_token ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
        ALTER TABLE refresh_token ALTER COLUMN created_at SET DEFAULT now();
    END IF;
END $$;
