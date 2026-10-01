package com.grecomilani.vehiclesales.infrastructure.persistence;

import com.grecomilani.vehiclesales.application.port.SaleRepository;
import com.grecomilani.vehiclesales.domain.sale.Sale;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaSaleRepositoryAdapter implements SaleRepository {

    private final SpringDataSaleRepository repository;

    JpaSaleRepositoryAdapter(SpringDataSaleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Sale save(Sale sale) {
        return repository.save(SaleEntity.from(sale)).toDomain();
    }
}
