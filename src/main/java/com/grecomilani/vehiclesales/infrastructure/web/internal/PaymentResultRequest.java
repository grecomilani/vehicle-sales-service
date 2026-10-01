package com.grecomilani.vehiclesales.infrastructure.web.internal;

import com.grecomilani.vehiclesales.domain.sale.PaymentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Resultado do pagamento repassado pelo {@code vehicle-platform-api}.
 */
public record PaymentResultRequest(@NotNull PaymentResult status) {

    public enum PaymentResult {
        PAID(PaymentStatus.PAID),
        CANCELED(PaymentStatus.CANCELED);

        private final PaymentStatus paymentStatus;

        PaymentResult(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        PaymentStatus toPaymentStatus() {
            return paymentStatus;
        }
    }
}
