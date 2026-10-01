package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.application.port.SaleRepository;
import com.grecomilani.vehiclesales.application.port.TransactionRunner;
import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.sale.PaymentStatus;
import com.grecomilani.vehiclesales.domain.sale.Sale;
import com.grecomilani.vehiclesales.domain.sale.SaleNotFoundException;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleNotFoundException;

/**
 * Registra o resultado do pagamento de uma venda, a partir do código de pagamento.
 * <ul>
 *     <li>{@code PAID}: a venda é paga e o veículo passa a vendido;</li>
 *     <li>{@code CANCELED}: a venda é cancelada e o veículo volta a ficar disponível.</li>
 * </ul>
 * Reenvios do mesmo resultado (comuns em webhooks) não alteram nada.
 */
public class UpdatePaymentStatusUseCase {

    private final SaleRepository saleRepository;
    private final VehicleRepository vehicleRepository;
    private final TransactionRunner transactionRunner;

    public UpdatePaymentStatusUseCase(SaleRepository saleRepository, VehicleRepository vehicleRepository,
                                      TransactionRunner transactionRunner) {
        this.saleRepository = saleRepository;
        this.vehicleRepository = vehicleRepository;
        this.transactionRunner = transactionRunner;
    }

    public Sale execute(UpdatePaymentStatusCommand command) {
        return transactionRunner.inTransaction(() -> {
            Sale sale = saleRepository.findByPaymentCode(command.paymentCode())
                    .orElseThrow(() -> new SaleNotFoundException(command.paymentCode()));

            if (!sale.registerPaymentResult(command.result())) {
                return sale;
            }

            Vehicle vehicle = vehicleRepository.findById(sale.getVehicleId())
                    .orElseThrow(() -> new VehicleNotFoundException(sale.getVehicleId()));
            if (command.result() == PaymentStatus.PAID) {
                vehicle.markAsSold();
            } else {
                vehicle.release();
            }

            vehicleRepository.save(vehicle);
            return saleRepository.save(sale);
        });
    }
}
