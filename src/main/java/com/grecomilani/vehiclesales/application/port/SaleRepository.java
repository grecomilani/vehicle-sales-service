package com.grecomilani.vehiclesales.application.port;

import com.grecomilani.vehiclesales.domain.sale.Sale;

import java.util.Optional;

/**
 * Porta de persistência de vendas.
 */
public interface SaleRepository {

    Sale save(Sale sale);

    Optional<Sale> findByPaymentCode(String paymentCode);
}
