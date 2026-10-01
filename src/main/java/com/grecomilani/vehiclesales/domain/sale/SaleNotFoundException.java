package com.grecomilani.vehiclesales.domain.sale;

import com.grecomilani.vehiclesales.domain.exception.NotFoundException;

public class SaleNotFoundException extends NotFoundException {

    public SaleNotFoundException(String paymentCode) {
        super("Nenhuma venda encontrada para o código de pagamento %s.".formatted(paymentCode));
    }
}
