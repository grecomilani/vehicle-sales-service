package com.grecomilani.vehiclesales.infrastructure.web.internal;

import com.grecomilani.vehiclesales.application.usecase.SyncVehicleCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dados do veículo enviados pelo {@code vehicle-platform-api}.
 */
public record VehicleSyncRequest(
        @NotBlank @Size(max = 100) String brand,
        @NotBlank @Size(max = 100) String model,
        @NotNull @Min(1900) Integer year,
        @NotBlank @Size(max = 50) String color,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal price) {

    SyncVehicleCommand toCommand(UUID id) {
        return new SyncVehicleCommand(id, brand, model, year, color, price);
    }
}
