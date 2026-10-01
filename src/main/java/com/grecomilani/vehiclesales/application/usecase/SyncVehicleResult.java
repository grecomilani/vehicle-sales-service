package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;

/**
 * @param vehicle veículo após a sincronização
 * @param created {@code true} se o veículo foi criado, {@code false} se foi atualizado
 */
public record SyncVehicleResult(Vehicle vehicle, boolean created) {
}
