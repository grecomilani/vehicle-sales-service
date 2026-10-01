package com.grecomilani.vehiclesales.application.port;

import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência de veículos.
 */
public interface VehicleRepository {

    Optional<Vehicle> findById(UUID id);

    Vehicle save(Vehicle vehicle);
}
