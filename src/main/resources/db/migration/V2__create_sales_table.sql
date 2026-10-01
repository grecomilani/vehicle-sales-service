CREATE TABLE sales (
    id             UUID          PRIMARY KEY,
    vehicle_id     UUID          NOT NULL REFERENCES vehicles (id),
    buyer_cpf      VARCHAR(11)   NOT NULL,
    sale_date      DATE          NOT NULL,
    price          NUMERIC(12,2) NOT NULL CHECK (price > 0),
    payment_code   VARCHAR(36)   NOT NULL UNIQUE,
    payment_status VARCHAR(20)   NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'CANCELED')),
    version        BIGINT        NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ   NOT NULL
);

-- Garante no banco que um veículo tenha no máximo uma venda ativa
-- (pendente ou paga); vendas canceladas não contam.
CREATE UNIQUE INDEX uq_sales_active_vehicle
    ON sales (vehicle_id)
    WHERE payment_status IN ('PENDING', 'PAID');
