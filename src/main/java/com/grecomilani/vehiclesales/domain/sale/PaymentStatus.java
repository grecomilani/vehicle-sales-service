package com.grecomilani.vehiclesales.domain.sale;

public enum PaymentStatus {

    /** Aguardando a confirmação da entidade processadora de pagamentos. */
    PENDING,

    /** Pagamento efetuado. */
    PAID,

    /** Pagamento cancelado. */
    CANCELED
}
