

-- V16: Tabela para garantia de idempotência no consumo de eventos Kafka (ADR 011 / BE-018)
CREATE TABLE processed_event (
    event_id     UUID NOT NULL,
    consumer_id  VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_processed_event PRIMARY KEY (event_id, consumer_id)
);

CREATE INDEX idx_processed_event_processed_at ON processed_event (processed_at);
