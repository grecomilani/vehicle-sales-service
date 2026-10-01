package com.grecomilani.vehiclesales.infrastructure.persistence;

import com.grecomilani.vehiclesales.application.port.SaleRepository;
import com.grecomilani.vehiclesales.domain.sale.Sale;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
class JpaSaleRepositoryAdapter implements SaleRepository {

    private final SpringDataSaleRepository repository;

    JpaSaleRepositoryAdapter(SpringDataSaleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Sale save(Sale sale) {
        return repository.saveAndFlush(SaleEntity.from(sale)).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sale> findByPaymentCode(String paymentCode) {
        return repository.findByPaymentCode(paymentCode).map(SaleEntity::toDomain);
    }
}
