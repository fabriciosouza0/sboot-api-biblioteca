-- V8: Row Level Security (RLS) para multi-tenant (ADR 012)
-- Políticas de isolamento no nível do banco
-- Requer: SET LOCAL app.current_institution_id = '<uuid>' no início da transação

-- ==== Função helper para obter institution_id da sessão ====
CREATE OR REPLACE FUNCTION current_institution_id()
RETURNS UUID
LANGUAGE sql
STABLE
AS $$
    SELECT NULLIF(current_setting('app.current_institution_id', true), '')::UUID;
$$;

-- ==== Função para verificar se é global_admin ====
CREATE OR REPLACE FUNCTION is_global_admin()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT current_setting('app.is_global_admin', true) = 'true';
$$;

-- ==== Habilita RLS nas tabelas de domínio ====
ALTER TABLE institution           ENABLE ROW LEVEL SECURITY;
ALTER TABLE profile_config        ENABLE ROW LEVEL SECURITY;
ALTER TABLE library               ENABLE ROW LEVEL SECURITY;
ALTER TABLE patron                ENABLE ROW LEVEL SECURITY;
ALTER TABLE work                  ENABLE ROW LEVEL SECURITY;
ALTER TABLE item                  ENABLE ROW LEVEL SECURITY;
ALTER TABLE loan                  ENABLE ROW LEVEL SECURITY;
ALTER TABLE hold                  ENABLE ROW LEVEL SECURITY;
ALTER TABLE fine                  ENABLE ROW LEVEL SECURITY;
ALTER TABLE outbox_event          ENABLE ROW LEVEL SECURITY;

-- ==== Políticas: GLOBAL_ADMIN vê tudo; INSTITUTION_ADMIN vê apenas sua instituição ====

-- institution: GLOBAL_ADMIN vê todas; INSTITUTION_ADMIN vê apenas a sua
CREATE POLICY institution_select ON institution
    FOR SELECT USING (is_global_admin() OR id = current_institution_id());

CREATE POLICY institution_modify ON institution
    FOR ALL USING (is_global_admin()) WITH CHECK (is_global_admin());

-- profile_config
CREATE POLICY profile_config_select ON profile_config
    FOR SELECT USING (is_global_admin() OR institution_id = current_institution_id());

CREATE POLICY profile_config_modify ON profile_config
    FOR ALL USING (is_global_admin() OR institution_id = current_institution_id())
    WITH CHECK (is_global_admin() OR institution_id = current_institution_id());

-- library
CREATE POLICY library_select ON library
    FOR SELECT USING (is_global_admin() OR institution_id = current_institution_id());

CREATE POLICY library_modify ON library
    FOR ALL USING (is_global_admin() OR institution_id = current_institution_id())
    WITH CHECK (is_global_admin() OR institution_id = current_institution_id());

-- patron
CREATE POLICY patron_select ON patron
    FOR SELECT USING (is_global_admin() OR institution_id = current_institution_id());

CREATE POLICY patron_modify ON patron
    FOR ALL USING (is_global_admin() OR institution_id = current_institution_id())
    WITH CHECK (is_global_admin() OR institution_id = current_institution_id());

-- work
CREATE POLICY work_select ON work
    FOR SELECT USING (is_global_admin() OR institution_id = current_institution_id());

CREATE POLICY work_modify ON work
    FOR ALL USING (is_global_admin() OR institution_id = current_institution_id())
    WITH CHECK (is_global_admin() OR institution_id = current_institution_id());

-- item (via library -> institution)
CREATE POLICY item_select ON item
    FOR SELECT USING (
        is_global_admin() OR
        library_id IN (SELECT id FROM library WHERE institution_id = current_institution_id())
    );

CREATE POLICY item_modify ON item
    FOR ALL USING (
        is_global_admin() OR
        library_id IN (SELECT id FROM library WHERE institution_id = current_institution_id())
    )
    WITH CHECK (
        is_global_admin() OR
        library_id IN (SELECT id FROM library WHERE institution_id = current_institution_id())
    );

-- loan (via patron -> institution)
CREATE POLICY loan_select ON loan
    FOR SELECT USING (
        is_global_admin() OR
        patron_id IN (SELECT id FROM patron WHERE institution_id = current_institution_id())
    );

CREATE POLICY loan_modify ON loan
    FOR ALL USING (
        is_global_admin() OR
        patron_id IN (SELECT id FROM patron WHERE institution_id = current_institution_id())
    )
    WITH CHECK (
        is_global_admin() OR
        patron_id IN (SELECT id FROM patron WHERE institution_id = current_institution_id())
    );

-- hold (via patron -> institution)
CREATE POLICY hold_select ON hold
    FOR SELECT USING (
        is_global_admin() OR
        patron_id IN (SELECT id FROM patron WHERE institution_id = current_institution_id())
    );

CREATE POLICY hold_modify ON hold
    FOR ALL USING (
        is_global_admin() OR
        patron_id IN (SELECT id FROM patron WHERE institution_id = current_institution_id())
    )
    WITH CHECK (
        is_global_admin() OR
        patron_id IN (SELECT id FROM patron WHERE institution_id = current_institution_id())
    );

-- fine (via patron -> institution)
CREATE POLICY fine_select ON fine
    FOR SELECT USING (
        is_global_admin() OR
        patron_id IN (SELECT id FROM patron WHERE institution_id = current_institution_id())
    );

CREATE POLICY fine_modify ON fine
    FOR ALL USING (
        is_global_admin() OR
        patron_id IN (SELECT id FROM patron WHERE institution_id = current_institution_id())
    )
    WITH CHECK (
        is_global_admin() OR
        patron_id IN (SELECT id FROM patron WHERE institution_id = current_institution_id())
    );

-- outbox_event (global)
CREATE POLICY outbox_select ON outbox_event
    FOR SELECT USING (is_global_admin());

CREATE POLICY outbox_modify ON outbox_event
    FOR ALL USING (is_global_admin()) WITH CHECK (is_global_admin());

-- ==== Políticas para adms (gerenciamento de admins) ====
ALTER TABLE adms ENABLE ROW LEVEL SECURITY;

CREATE POLICY adms_select ON adms
    FOR SELECT USING (
        is_global_admin() OR
        codigo = (SELECT current_setting('app.current_adm_codigo', true))::INTEGER OR
        institution_id = current_institution_id() OR
        codigo IN (SELECT adm_codigo FROM adm_institution WHERE institution_id = current_institution_id())
    );

CREATE POLICY adms_modify ON adms
    FOR ALL USING (is_global_admin()) WITH CHECK (is_global_admin());

-- adm_institution
ALTER TABLE adm_institution ENABLE ROW LEVEL SECURITY;

CREATE POLICY adm_institution_select ON adm_institution
    FOR SELECT USING (
        is_global_admin() OR
        institution_id = current_institution_id()
    );

CREATE POLICY adm_institution_modify ON adm_institution
    FOR ALL USING (is_global_admin()) WITH CHECK (is_global_admin());

-- ==== Comentários ====
COMMENT ON FUNCTION current_institution_id() IS 'Retorna o institution_id definido via SET LOCAL app.current_institution_id';
COMMENT ON FUNCTION is_global_admin() IS 'Retorna true se a sessão for GLOBAL_ADMIN (SET LOCAL app.is_global_admin = true)';