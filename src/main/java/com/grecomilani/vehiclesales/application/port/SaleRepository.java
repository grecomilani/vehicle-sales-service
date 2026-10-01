package com.grecomilani.vehiclesales.application.port;

import com.grecomilani.vehiclesales.domain.sale.Sale;

/**
 * Porta de persistência de vendas.
 */
public interface SaleRepository {

    Sale save(Sale sale);
}
