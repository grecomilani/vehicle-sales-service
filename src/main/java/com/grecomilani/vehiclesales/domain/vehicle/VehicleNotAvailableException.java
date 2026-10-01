package com.grecomilani.vehiclesales.domain.vehicle;

import com.grecomilani.vehiclesales.domain.exception.ConflictException;

import java.util.UUID;

/**
 * Operação que exige um veículo disponível para venda foi solicitada
 * para um veículo reservado ou vendido.
 */
public class VehicleNotAvailableException extends ConflictException {

    public VehicleNotAvailableException(UUID vehicleId, VehicleStatus currentStatus) {
        super("O veículo %s não está disponível para venda (status atual: %s)."
                .formatted(vehicleId, currentStatus));
    }
}
