-- Safe, additive migration: creates ticket_feedback only when it is missing.
-- Does not drop or modify existing tables or rows.
BEGIN;

CREATE TABLE IF NOT EXISTS ticket_feedback (
    id              BIGINT       PRIMARY KEY,
    puntuacion      INTEGER      NOT NULL,
    comentario      VARCHAR(500),
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_feedback_ticket
        FOREIGN KEY (id) REFERENCES tickets (id) ON DELETE CASCADE,
    CONSTRAINT ck_feedback_puntuacion CHECK (puntuacion BETWEEN 1 AND 5)
);

-- Match the Java entity's Integer field when the table already existed.
ALTER TABLE ticket_feedback
    ALTER COLUMN puntuacion TYPE INTEGER USING puntuacion::INTEGER;

COMMIT;
