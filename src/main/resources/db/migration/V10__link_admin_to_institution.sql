-- V10: Vincular admin legado à instituição piloto + fallback /me
-- O admin original (V1) não tinha institution_id. V6 adicionou a coluna mas não atualizou o seed.
-- Além disso, adm_institution (V7) permite multi-tenancy; /me deve consultar essa tabela.

-- ==== Vincular admin ao EEEP-JBL via adm_institution ====
INSERT INTO adm_institution (adm_codigo, institution_id)
SELECT a.codigo, i.id
FROM adms a, institution i
WHERE a.login = '000.000.000-00'
  AND i.code = 'EEEP-JBL'
  AND NOT EXISTS (
      SELECT 1 FROM adm_institution ai
      WHERE ai.adm_codigo = a.codigo AND ai.institution_id = i.id
  );
