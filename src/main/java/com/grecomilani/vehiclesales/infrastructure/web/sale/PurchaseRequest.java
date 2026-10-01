package com.grecomilani.vehiclesales.infrastructure.web.sale;

import com.grecomilani.vehiclesales.application.usecase.PurchaseVehicleCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @param vehicleId veículo comprado
 * @param buyerCpf  CPF de quem comprou, com ou sem máscara
 * @param saleDate  data da venda (não pode estar no futuro)
 */
public record PurchaseRequest(
        @NotNull UUID vehicleId,
        @NotBlank @CPF String buyerCpf,
        @NotNull LocalDate saleDate) {

    PurchaseVehicleCommand toCommand() {
        return new PurchaseVehicleCommand(vehicleId, buyerCpf, saleDate);
    }
}
