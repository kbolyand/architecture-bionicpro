CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TABLE prostheses (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), user_id UUID NOT NULL, serial_number VARCHAR(100) NOT NULL UNIQUE, model VARCHAR(100) NOT NULL, manufacturer VARCHAR(200), installed_at DATE, status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX idx_prostheses_user_id ON prostheses(user_id);
