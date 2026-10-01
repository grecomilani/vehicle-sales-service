CREATE TABLE vehicles (
    id               UUID          PRIMARY KEY,
    brand            VARCHAR(100)  NOT NULL,
    model            VARCHAR(100)  NOT NULL,
    manufacture_year INTEGER       NOT NULL,
    color            VARCHAR(50)   NOT NULL,
    price            NUMERIC(12,2) NOT NULL CHECK (price > 0),
    status           VARCHAR(20)   NOT NULL CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD')),
    version          BIGINT        NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ   NOT NULL
);

-- Atende às listagens de veículos à venda e vendidos, ordenadas por preço.
CREATE INDEX idx_vehicles_status_price ON vehicles (status, price);
