package com.grecomilani.vehiclesales.application.usecase;

import java.math.BigDecimal;
import java.util.UUID;

public record SyncVehicleCommand(
        UUID id,
        String brand,
        String model,
        int year,
        String color,
        BigDecimal price) {
}
