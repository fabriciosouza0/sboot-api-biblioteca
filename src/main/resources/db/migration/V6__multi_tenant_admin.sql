-- V6: Multi-tenant admin support (ADR 012)
-- Extends adms table with role and institution_id for tenant isolation

-- ==== Enum para roles de admin ====
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'admin_role') THEN
        CREATE TYPE admin_role AS ENUM ('GLOBAL_ADMIN', 'INSTITUTION_ADMIN');
    END IF;
END $$;

-- ==== ALTER adms: role + institution_id ====
ALTER TABLE adms
    ADD COLUMN IF NOT EXISTS role admin_role NOT NULL DEFAULT 'INSTITUTION_ADMIN',
    ADD COLUMN IF NOT EXISTS institution_id UUID REFERENCES institution (id);

-- Unique constraint necessária para ON CONFLICT no seed
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_adms_login') THEN
        ALTER TABLE adms ADD CONSTRAINT uk_adms_login UNIQUE (login);
    END IF;
END $$;

-- Index para queries por instituição
CREATE INDEX IF NOT EXISTS idx_adms_institution_id ON adms (institution_id);

-- ==== Seed: Global admin padrão (sem institution_id = acesso total) ====
-- Senha: 'admin123' (BCrypt hash gerado no startup via DataInitializer se necessário)
INSERT INTO adms (login, senha, nome, role, institution_id)
VALUES ('global-admin', '$2a$10$XURPShQIWJ7Z8Y7J7J7J7u7J7J7J7J7J7J7J7J7J7J7J7J7J7J7J7', 'Global Admin', 'GLOBAL_ADMIN', NULL)
ON CONFLICT (login) DO UPDATE SET
    role = EXCLUDED.role,
    institution_id = EXCLUDED.institution_id,
    nome = EXCLUDED.nome;

-- ==== Comentários ====
COMMENT ON COLUMN adms.role IS 'GLOBAL_ADMIN = acesso a todas as instituições; INSTITUTION_ADMIN = apenas instituição vinculada';
COMMENT ON COLUMN adms.institution_id IS 'NULL para GLOBAL_ADMIN; FK para institution para INSTITUTION_ADMIN';