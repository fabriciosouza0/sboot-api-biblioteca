-- V4: Domínio de Circulação (ADR 010) + Transactional Outbox (ADR 011)
-- Novas tabelas: institution, profile_config, library, patron, work, item, loan, hold, fine, outbox_event
-- ALTER: turma.institution_id, locatario.institution_id (para migração V5)

-- ==== Institution ====
CREATE TABLE institution (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(20) NOT NULL,
    name       VARCHAR(120) NOT NULL,
    settings   TEXT NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_institution_code UNIQUE (code)
);

-- ==== Profile Config (por instituição) ====
CREATE TABLE profile_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL REFERENCES institution (id),
    profile         VARCHAR(20) NOT NULL,
    max_loans       INTEGER NOT NULL,
    loan_days       INTEGER NOT NULL,
    max_renewals    INTEGER NOT NULL,
    hold_limit      INTEGER NOT NULL,
    fine_rate_cents INTEGER NOT NULL DEFAULT 50,
    fine_cap_cents  INTEGER NOT NULL DEFAULT 5000,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_profile_config UNIQUE (institution_id, profile),
    CONSTRAINT ck_profile_config_profile CHECK (profile IN ('STUDENT','TEACHER','STAFF','EXTERNAL'))
);

-- ==== Library ====
CREATE TABLE library (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id UUID NOT NULL REFERENCES institution (id),
    name           VARCHAR(120) NOT NULL,
    is_central     BOOLEAN NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_library_institution_name UNIQUE (institution_id, name)
);

-- ==== Patron ====
CREATE TABLE patron (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id UUID NOT NULL REFERENCES institution (id),
    external_id    VARCHAR(30) NOT NULL,
    name           VARCHAR(120) NOT NULL,
    phone          VARCHAR(20),
    profile        VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    fine_balance   INTEGER NOT NULL DEFAULT 0,
    version        BIGINT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_patron_external UNIQUE (institution_id, external_id),
    CONSTRAINT ck_patron_profile CHECK (profile IN ('STUDENT','TEACHER','STAFF','EXTERNAL')),
    CONSTRAINT ck_patron_status CHECK (status IN ('ACTIVE','BLOCKED','SUSPENDED'))
);

-- ==== Work (catálogo bibliográfico) ====
CREATE TABLE work (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id UUID NOT NULL REFERENCES institution (id),
    isbn13         VARCHAR(20),
    title          VARCHAR(255) NOT NULL,
    authors        TEXT NOT NULL DEFAULT '[]',
    publisher      VARCHAR(120),
    published_year INTEGER,
    edition        VARCHAR(30),
    cdu            VARCHAR(30),
    cover_url      VARCHAR(500),
    description    TEXT,
    version        BIGINT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_work_institution_isbn UNIQUE (institution_id, isbn13)
);

-- ==== Item (exemplar físico) ====
CREATE TABLE item (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    work_id        UUID NOT NULL REFERENCES work (id),
    library_id     UUID NOT NULL REFERENCES library (id),
    barcode        VARCHAR(40) NOT NULL,
    call_number    VARCHAR(60),
    status         VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version        BIGINT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_item_barcode UNIQUE (barcode),
    CONSTRAINT ck_item_status CHECK (status IN ('AVAILABLE','ON_LOAN','ON_HOLD','IN_TRANSIT','LOST','WITHDRAWN','IN_REPAIR'))
);

-- ==== Loan (empréstimo) ====
CREATE TABLE loan (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patron_id            UUID NOT NULL REFERENCES patron (id),
    item_id              UUID NOT NULL REFERENCES item (id),
    library_id           UUID NOT NULL REFERENCES library (id),
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    checked_out_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    due_at               TIMESTAMPTZ NOT NULL,
    returned_at          TIMESTAMPTZ,
    returned_library_id  UUID REFERENCES library (id),
    renewal_count        INTEGER NOT NULL DEFAULT 0,
    version              BIGINT NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_loan_status CHECK (status IN ('ACTIVE','RETURNED','OVERDUE','LOST_CLAIMED'))
);

-- Unique active loan per item
CREATE UNIQUE INDEX uq_loan_active_item ON loan (item_id) WHERE status IN ('ACTIVE','OVERDUE');
CREATE INDEX idx_loan_patron ON loan (patron_id) WHERE status IN ('ACTIVE','OVERDUE');
CREATE INDEX idx_loan_due ON loan (due_at) WHERE status IN ('ACTIVE','OVERDUE');

-- ==== Hold (reserva) ====
CREATE TABLE hold (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patron_id    UUID NOT NULL REFERENCES patron (id),
    work_id      UUID NOT NULL REFERENCES work (id),
    library_id   UUID NOT NULL REFERENCES library (id),
    status       VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    position     INTEGER NOT NULL,
    placed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    ready_at     TIMESTAMPTZ,
    expires_at   TIMESTAMPTZ,
    fulfilled_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    version      BIGINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_hold_status CHECK (status IN ('WAITING','READY','FULFILLED','EXPIRED','CANCELLED'))
);

-- Unique position per work+library among waiting holds
CREATE UNIQUE INDEX uq_hold_position ON hold (work_id, library_id, position) WHERE status = 'WAITING';

-- ==== Fine (multa) ====
CREATE TABLE fine (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patron_id     UUID NOT NULL REFERENCES patron (id),
    loan_id       UUID REFERENCES loan (id),
    type          VARCHAR(20) NOT NULL,
    amount_cents  INTEGER NOT NULL,
    balance_cents INTEGER NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    assessed_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    paid_at       TIMESTAMPTZ,
    waived_at     TIMESTAMPTZ,
    reason        VARCHAR(255),
    version       BIGINT NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_fine_type CHECK (type IN ('OVERDUE','LOST','DAMAGE','PROCESSING')),
    CONSTRAINT ck_fine_status CHECK (status IN ('PENDING','PARTIAL','PAID','WAIVED','WRITTEN_OFF'))
);

CREATE INDEX idx_fine_patron_status ON fine (patron_id, status) WHERE status IN ('PENDING','PARTIAL');

-- ==== Outbox Event (ADR 011) ====
CREATE TABLE outbox_event (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(40) NOT NULL,
    aggregate_id   UUID NOT NULL,
    event_type     VARCHAR(60) NOT NULL,
    payload        TEXT NOT NULL,
    metadata       TEXT NOT NULL DEFAULT '{}',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at   TIMESTAMPTZ
);
CREATE INDEX idx_outbox_unprocessed ON outbox_event (created_at) WHERE processed_at IS NULL;

-- ==== ALTER turma: institution_id (Option A) ====
ALTER TABLE turma ADD COLUMN institution_id UUID REFERENCES institution (id);

-- ==== ALTER locatario: institution_id (para migração V5) ====
ALTER TABLE locatario ADD COLUMN institution_id UUID REFERENCES institution (id);

-- ==== Trigger: updated_at ====
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_institution_updated_at   BEFORE UPDATE ON institution   FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_profile_config_updated_at BEFORE UPDATE ON profile_config FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_library_updated_at       BEFORE UPDATE ON library       FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_patron_updated_at        BEFORE UPDATE ON patron        FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_work_updated_at          BEFORE UPDATE ON work          FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_item_updated_at          BEFORE UPDATE ON item          FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_loan_updated_at          BEFORE UPDATE ON loan          FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_hold_updated_at          BEFORE UPDATE ON hold          FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_fine_updated_at          BEFORE UPDATE ON fine          FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- ==== Trigger: patron.fine_balance denormalizado ====
CREATE OR REPLACE FUNCTION fn_update_patron_fine_balance()
RETURNS TRIGGER AS $$
DECLARE
    v_patron_id UUID;
    v_balance INTEGER;
BEGIN
    v_patron_id := COALESCE(NEW.patron_id, OLD.patron_id);
    SELECT COALESCE(SUM(balance_cents), 0) INTO v_balance
    FROM fine WHERE patron_id = v_patron_id AND status IN ('PENDING','PARTIAL');
    UPDATE patron SET fine_balance = v_balance WHERE id = v_patron_id;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_fine_balance_insert
AFTER INSERT ON fine FOR EACH ROW EXECUTE FUNCTION fn_update_patron_fine_balance();

CREATE TRIGGER trg_fine_balance_update
AFTER UPDATE OF balance_cents, status, patron_id ON fine
FOR EACH ROW EXECUTE FUNCTION fn_update_patron_fine_balance();

CREATE TRIGGER trg_fine_balance_delete
AFTER DELETE ON fine FOR EACH ROW EXECUTE FUNCTION fn_update_patron_fine_balance();

-- ==== Seed: Instituição piloto + profile_config defaults ====
INSERT INTO institution (code, name)
VALUES ('EEEP-JBL', 'E.E.E.P. Pe. João Bosco de Lima')
ON CONFLICT (code) DO NOTHING;

INSERT INTO profile_config (institution_id, profile, max_loans, loan_days, max_renewals, hold_limit, fine_rate_cents, fine_cap_cents)
SELECT i.id, p.profile, p.max_loans, p.loan_days, p.max_renewals, p.hold_limit, p.fine_rate_cents, p.fine_cap_cents
FROM institution i
CROSS JOIN (VALUES
    ('STUDENT', 3, 7, 2, 5, 50, 5000),
    ('TEACHER', 10, 30, 3, 10, 50, 5000),
    ('STAFF', 999, 60, 999, 999, 0, 0),
    ('EXTERNAL', 2, 14, 1, 3, 100, 10000)
) AS p(profile, max_loans, loan_days, max_renewals, hold_limit, fine_rate_cents, fine_cap_cents)
WHERE i.code = 'EEEP-JBL'
ON CONFLICT DO NOTHING;

-- ==== Backfill turma.institution_id (piloto) ====
UPDATE turma SET institution_id = (SELECT id FROM institution WHERE code = 'EEEP-JBL');