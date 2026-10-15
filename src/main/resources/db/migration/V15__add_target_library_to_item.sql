-- V15: Add target_library_id to item for inter-library transfers
ALTER TABLE item
    ADD COLUMN IF NOT EXISTS target_library_id UUID REFERENCES library (id);

CREATE INDEX IF NOT EXISTS idx_item_target_library_id ON item (target_library_id);

COMMENT ON COLUMN item.target_library_id IS 'Filial de destino pretendida quando o exemplar está em trânsito (IN_TRANSIT)';
