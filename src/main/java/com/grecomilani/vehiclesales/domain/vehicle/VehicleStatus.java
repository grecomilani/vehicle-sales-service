package com.grecomilani.vehiclesales.domain.vehicle;

public enum VehicleStatus {

    /** Disponível para venda. */
    AVAILABLE,

    /** Compra iniciada, aguardando a confirmação do pagamento. */
    RESERVED,

    /** Pagamento confirmado. */
    SOLD
}
