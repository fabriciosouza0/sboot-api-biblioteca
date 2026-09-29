-- V9: Adicionar coluna ready_expired na tabela hold
-- O modelo de domínio Hold.java define readyExpired mas a V4 não incluiu a coluna

ALTER TABLE hold ADD COLUMN ready_expired BOOLEAN NOT NULL DEFAULT false;
