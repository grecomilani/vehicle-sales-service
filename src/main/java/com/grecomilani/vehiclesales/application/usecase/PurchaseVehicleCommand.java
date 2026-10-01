package com.grecomilani.vehiclesales.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

public record PurchaseVehicleCommand(UUID vehicleId, String buyerCpf, LocalDate saleDate) {
}
