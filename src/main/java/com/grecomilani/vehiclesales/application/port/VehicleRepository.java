package com.grecomilani.vehiclesales.application.port;

import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência de veículos.
 */
public interface VehicleRepository {

    Optional<Vehicle> findById(UUID id);

    Vehicle save(Vehicle vehicle);

    /**
     * Busca os veículos com o status informado, ordenados por preço crescente
     * (e pelo {@code id} em caso de empate, para uma paginação estável).
     */
    PageResult<Vehicle> findByStatusOrderByPriceAsc(VehicleStatus status, int page, int size);
}
