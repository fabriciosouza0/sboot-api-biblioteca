-- V13: Suporte a Catálogo Global (obras com institution_id NULL) - ADR 012
-- Permite que obras compartilhadas/nacionais não fiquem restritas a uma única instituição
-- e ajusta a política de Row-Level Security (RLS) para permitir que qualquer instituição visualize obras globais.

ALTER TABLE work ALTER COLUMN institution_id DROP NOT NULL;

DROP POLICY IF EXISTS work_select ON work;
CREATE POLICY work_select ON work
    FOR SELECT USING (
        is_global_admin() OR
        institution_id = current_institution_id() OR
        institution_id IS NULL
    );
