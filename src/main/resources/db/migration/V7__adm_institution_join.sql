-- V7: Adm-Institution many-to-many join table
-- Permite INSTITUTION_ADMIN gerenciar múltiplas instituições

-- ==== Tabela de junção adm_institution ====
CREATE TABLE IF NOT EXISTS adm_institution (
    adm_codigo    INTEGER NOT NULL REFERENCES adms (codigo) ON DELETE CASCADE,
    institution_id UUID   NOT NULL REFERENCES institution (id) ON DELETE CASCADE,
    assigned_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_by   INTEGER REFERENCES adms (codigo),
    PRIMARY KEY (adm_codigo, institution_id)
);

CREATE INDEX IF NOT EXISTS idx_adm_institution_institution ON adm_institution (institution_id);

-- ==== Trigger updated_at ====
CREATE TRIGGER trg_adm_institution_updated_at
    BEFORE UPDATE ON adm_institution
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- ==== Comentários ====
COMMENT ON TABLE adm_institution IS 'Vincula INSTITUTION_ADMIN a uma ou mais instituições';
COMMENT ON COLUMN adm_institution.assigned_by IS 'Admin que fez a vinculação (auditoria)';