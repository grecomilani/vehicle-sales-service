package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;

import java.util.Optional;

/**
 * Sincroniza um veículo cadastrado ou editado no {@code vehicle-platform-api}.
 * <p>
 * Operação idempotente: cria o veículo se ele ainda não existir neste serviço
 * ou atualiza seus dados caso já exista.
 */
public class SyncVehicleUseCase {

    private final VehicleRepository vehicleRepository;

    public SyncVehicleUseCase(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public SyncVehicleResult execute(SyncVehicleCommand command) {
        Optional<Vehicle> existing = vehicleRepository.findById(command.id());

        Vehicle vehicle = existing
                .map(current -> {
                    current.updateDetails(command.brand(), command.model(), command.year(),
                            command.color(), command.price());
                    return current;
                })
                .orElseGet(() -> Vehicle.create(command.id(), command.brand(), command.model(),
                        command.year(), command.color(), command.price()));

        return new SyncVehicleResult(vehicleRepository.save(vehicle), existing.isEmpty());
    }
}
