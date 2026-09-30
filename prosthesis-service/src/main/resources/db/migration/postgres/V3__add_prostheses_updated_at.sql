ALTER TABLE prostheses ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
CREATE INDEX idx_prostheses_updated_at ON prostheses(updated_at);