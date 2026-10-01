package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.domain.sale.PaymentStatus;

/**
 * @param paymentCode código de pagamento gerado na compra
 * @param result      resultado informado pela entidade processadora (PAID ou CANCELED)
 */
public record UpdatePaymentStatusCommand(String paymentCode, PaymentStatus result) {
}
