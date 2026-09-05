-- V3: Índices GIN para Full-Text Search (busca por nome/título/descrição)
-- Stemming português + prefixo via websearch_to_tsquery (:term || ':*')
-- Índice expression GIN: a query deve usar a mesma expressão exata (to_tsvector('portuguese', col))

CREATE INDEX idx_livro_titulo_fts ON livro USING GIN (to_tsvector('portuguese', titulo));

CREATE INDEX idx_locatario_nome_fts ON locatario USING GIN (to_tsvector('portuguese', nome));

CREATE INDEX idx_autor_nome_fts ON autor USING GIN (to_tsvector('portuguese', nome));

CREATE INDEX idx_cdd_descricao_fts ON cdd USING GIN (to_tsvector('portuguese', descricao));
