package com.grecomilani.vehiclesales.infrastructure.persistence;

import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
class JpaVehicleRepositoryAdapter implements VehicleRepository {

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
}
