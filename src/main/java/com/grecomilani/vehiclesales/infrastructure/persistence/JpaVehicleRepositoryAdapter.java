package com.grecomilani.vehiclesales.infrastructure.persistence;

import com.grecomilani.vehiclesales.application.port.PageResult;
import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
class JpaVehicleRepositoryAdapter implements VehicleRepository {

    private static final Sort BY_PRICE_ASC = Sort.by(Sort.Order.asc("price"), Sort.Order.asc("id"));

    private final SpringDataVehicleRepository repository;

    JpaVehicleRepositoryAdapter(SpringDataVehicleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle> findById(UUID id) {
        return repository.findById(id).map(VehicleEntity::toDomain);
    }

    @Override
    @Transactional
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = repository.findById(vehicle.getId())
                .map(existing -> {
                    existing.copyFrom(vehicle);
                    return existing;
                })
                .orElseGet(() -> VehicleEntity.from(vehicle));

        return repository.save(entity).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Vehicle> findByStatusOrderByPriceAsc(VehicleStatus status, int page, int size) {
        Page<VehicleEntity> result = repository.findByStatus(status, PageRequest.of(page, size, BY_PRICE_ASC));
        return new PageResult<>(result.map(VehicleEntity::toDomain).getContent(),
                page, size, result.getTotalElements());
    }
}
