-- V5: Migração de dados legados → domínio de circulação (ADR 010)
-- Garante ZERO locação órfã, barcode sequencial por quantidade

-- ==== 0. Validação: ZERO locação órfã ====
DO $$
DECLARE orphan_count INT;
BEGIN
    SELECT COUNT(*) INTO orphan_count
    FROM loca l
    LEFT JOIN locatario lt ON lt.cpf = l.cpf_locatario
    WHERE lt.cpf IS NULL;
    IF orphan_count > 0 THEN
        RAISE EXCEPTION 'Migração abortada: % locações órfãs (sem locatário correspondente)', orphan_count;
    END IF;
END $$;

-- ==== 1. Backfill locatario.institution_id (Option A: via aluno.turma; professores → instituição piloto) ====
UPDATE locatario lt
SET institution_id = t.institution_id
FROM aluno a
JOIN turma t ON t.codigo = a.codigo_turma
WHERE lt.codigo_aluno = a.codigo;

UPDATE locatario lt
SET institution_id = (SELECT id FROM institution WHERE code = 'EEEP-JBL')
WHERE lt.institution_id IS NULL;

-- ==== 2. Patron ← Locatario ====
INSERT INTO patron (institution_id, external_id, name, phone, profile, status, fine_balance, version)
SELECT lt.institution_id, lt.cpf, lt.nome, lt.telefone,
       CASE WHEN lt.codigo_aluno IS NOT NULL THEN 'STUDENT'
            WHEN lt.codigo_professor IS NOT NULL THEN 'TEACHER'
            ELSE 'EXTERNAL' END,
       'ACTIVE', 0, 0
FROM locatario lt
WHERE lt.institution_id IS NOT NULL;

-- ==== 3. Work ← Livro ====
INSERT INTO work (institution_id, isbn13, title, authors, publisher, published_year, edition, cdu, cover_url, description, version)
SELECT i.id,
       lv.codigo::text,
       lv.titulo,
       jsonb_build_array(jsonb_build_object('name', COALESCE(a.nome, 'Desconhecido'), 'role', 'author'))::text,
       NULL, NULL, NULL, c.codigo::text, NULL, NULL, 0
FROM livro lv
JOIN institution i ON i.code = 'EEEP-JBL'
LEFT JOIN autor a ON a.codigo = lv.codigo_autor
LEFT JOIN cdd c ON c.codigo = lv.codigo_cdd;

-- ==== 4. Library central ====
INSERT INTO library (institution_id, name, is_central)
SELECT i.id, i.name || ' - Central', true FROM institution i;

-- ==== 5. Item ← Livro (barcode sequencial por quantidade) ====
INSERT INTO item (work_id, library_id, barcode, call_number, status, version)
SELECT w.id, lib.id,
       w.isbn13 || '-' || LPAD(gs.n::text, 4, '0'),
       COALESCE(NULLIF(w.cdu, ''), '000') || ' ' ||
         SUBSTRING(COALESCE((w.authors::jsonb->0->>'name'), 'AAA') FROM 1 FOR 3) ||
         LPAD(gs.n::text, 3, '0'),
       'AVAILABLE', 0
FROM livro lv
JOIN work w ON w.isbn13 = lv.codigo::text
JOIN institution i ON i.code = 'EEEP-JBL'
JOIN library lib ON lib.institution_id = i.id AND lib.is_central = true
CROSS JOIN LATERAL generate_series(1, lv.qtd) AS gs(n);

-- ==== 6. Loan ← Loca ====
INSERT INTO loan (patron_id, item_id, library_id, status, checked_out_at, due_at, renewal_count, version)
SELECT p.id,
       it.id,
       lib.id,
       CASE WHEN l.atrasado THEN 'OVERDUE' ELSE 'ACTIVE' END,
       l.data_de_locacao,
       l.data_para_devolucao,
       0, 0
FROM loca l
JOIN locatario lt ON lt.cpf = l.cpf_locatario
JOIN patron p ON p.institution_id = lt.institution_id AND p.external_id = lt.cpf
JOIN livro lv ON lv.codigo = l.codigo_livro
JOIN work w ON w.isbn13 = lv.codigo::text
JOIN item it ON it.work_id = w.id AND it.barcode = w.isbn13 || '-0001'
JOIN institution i ON i.code = 'EEEP-JBL'
JOIN library lib ON lib.institution_id = i.id AND lib.is_central = true;

-- ==== 7. Fine (overdue) ← Loan overdue ====
INSERT INTO fine (patron_id, loan_id, type, amount_cents, balance_cents, status, assessed_at, reason, version)
SELECT p.id, ln.id, 'OVERDUE',
       LEAST((CURRENT_DATE - ln.due_at::date) * pc.fine_rate_cents, pc.fine_cap_cents),
       LEAST((CURRENT_DATE - ln.due_at::date) * pc.fine_rate_cents, pc.fine_cap_cents),
       'PENDING', now(), 'Migração legado', 0
FROM loan ln
JOIN patron p ON p.id = ln.patron_id
JOIN profile_config pc ON pc.institution_id = p.institution_id AND pc.profile = p.profile
WHERE ln.status = 'OVERDUE' AND ln.due_at::date < CURRENT_DATE;

-- ==== 8. Update patron.fine_balance + status BLOCKED (trigger já faz, mas força para legado) ====
UPDATE patron p SET fine_balance = COALESCE((
    SELECT SUM(balance_cents) FROM fine f WHERE f.patron_id = p.id
), 0);

UPDATE patron p SET status = 'BLOCKED' WHERE fine_balance > 2000;

-- ==== 9. Item status → ON_LOAN para itens em empréstimos ativos/overdue ====
UPDATE item it SET status = 'ON_LOAN'
FROM loan ln
WHERE it.id = ln.item_id AND ln.status IN ('ACTIVE','OVERDUE');