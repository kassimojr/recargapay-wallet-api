-- Migration V1: Criação das tabelas principais para carteira digital

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    balance NUMERIC(19,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT unique_user_wallet UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    amount NUMERIC(19,2) NOT NULL,
    type VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    related_user_id UUID
);

-- Massa de dados para testes/desenvolvimento
INSERT INTO users (id, email, name) VALUES
    ('11111111-1111-1111-1111-111111111111', 'alice@recarga.com', 'Alice'),
    ('22222222-2222-2222-2222-222222222222', 'bob@recarga.com', 'Bob'),
    ('33333333-3333-3333-3333-333333333333', 'carol@recarga.com', 'Carol');

INSERT INTO wallets (id, user_id, balance, created_at, updated_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 100.00, NOW(), NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 200.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 300.00, NOW(), NOW());

INSERT INTO transactions (id, wallet_id, amount, type, timestamp, related_user_id) VALUES
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 50.00, 'DEPOSIT', NOW(), NULL),
    ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 75.00, 'WITHDRAWAL', NOW(), NULL),
    ('ccccccc3-cccc-cccc-cccc-ccccccccccc3', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 120.00, 'TRANSFER', NOW(), '11111111-1111-1111-1111-111111111111');
