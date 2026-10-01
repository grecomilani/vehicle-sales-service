package com.grecomilani.vehiclesales.infrastructure.web.internal;

import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String brand,
        String model,
        int year,
        String color,
        BigDecimal price,
        VehicleStatus status) {

    static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(vehicle.getId(), vehicle.getBrand(), vehicle.getModel(),
                vehicle.getYear(), vehicle.getColor(), vehicle.getPrice(), vehicle.getStatus());
    }
}
