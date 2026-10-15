-- V14: Adiciona campos de observação de transferência e justificativa de baixa no exemplar
ALTER TABLE item ADD COLUMN transfer_note TEXT;
ALTER TABLE item ADD COLUMN withdraw_reason TEXT;
