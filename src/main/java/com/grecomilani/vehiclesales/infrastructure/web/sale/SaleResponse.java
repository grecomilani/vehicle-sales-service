package com.grecomilani.vehiclesales.infrastructure.web.sale;

import com.grecomilani.vehiclesales.domain.sale.PaymentStatus;
import com.grecomilani.vehiclesales.domain.sale.Sale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SaleResponse(
        UUID id,
        UUID vehicleId,
        String buyerCpf,
        LocalDate saleDate,
        BigDecimal price,
        String paymentCode,
        PaymentStatus paymentStatus) {

    static SaleResponse from(Sale sale) {
        return new SaleResponse(sale.getId(), sale.getVehicleId(), sale.getBuyerCpf().value(),
                sale.getSaleDate(), sale.getPrice(), sale.getPaymentCode(), sale.getPaymentStatus());
    }
}
