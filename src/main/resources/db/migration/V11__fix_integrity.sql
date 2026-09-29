-- V11: Correções de integridade
-- 1) Remover trigger inválido em adm_institution (tabela sem updated_at)
-- 2) Adicionar coluna updated_at em adm_institution para consistência

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_adm_institution_updated_at') THEN
        DROP TRIGGER trg_adm_institution_updated_at ON adm_institution;
    END IF;
END $$;

ALTER TABLE adm_institution ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE TRIGGER trg_adm_institution_updated_at
    BEFORE UPDATE ON adm_institution
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
