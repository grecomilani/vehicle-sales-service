package com.grecomilani.vehiclesales.domain.vehicle;

import com.grecomilani.vehiclesales.domain.exception.NotFoundException;

import java.util.UUID;

public class VehicleNotFoundException extends NotFoundException {

    public VehicleNotFoundException(UUID vehicleId) {
        super("Veículo %s não encontrado.".formatted(vehicleId));
    }
}
