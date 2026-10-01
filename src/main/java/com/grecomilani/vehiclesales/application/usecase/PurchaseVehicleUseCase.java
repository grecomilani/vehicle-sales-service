package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.application.port.SaleRepository;
import com.grecomilani.vehiclesales.application.port.TransactionRunner;
import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.sale.Cpf;
import com.grecomilani.vehiclesales.domain.sale.Sale;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleNotFoundException;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Efetua a compra de um veículo: reserva o veículo e registra a venda com o
 * pagamento pendente, na mesma transação.
 * <p>
 * Se duas compras do mesmo veículo acontecerem ao mesmo tempo, o controle de
 * concorrência otimista do veículo garante que apenas uma delas é efetivada.
 */
public class PurchaseVehicleUseCase {

    private final VehicleRepository vehicleRepository;
    private final SaleRepository saleRepository;
    private final TransactionRunner transactionRunner;
    private final Clock clock;

    public PurchaseVehicleUseCase(VehicleRepository vehicleRepository, SaleRepository saleRepository,
                                  TransactionRunner transactionRunner, Clock clock) {
        this.vehicleRepository = vehicleRepository;
        this.saleRepository = saleRepository;
        this.transactionRunner = transactionRunner;
        this.clock = clock;
    }

    public Sale execute(PurchaseVehicleCommand command) {
        Cpf buyerCpf = new Cpf(command.buyerCpf());

        return transactionRunner.inTransaction(() -> {
            Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
                    .orElseThrow(() -> new VehicleNotFoundException(command.vehicleId()));

            vehicle.reserve();
            Sale sale = Sale.start(vehicle, buyerCpf, command.saleDate(), LocalDate.now(clock));

            vehicleRepository.save(vehicle);
            return saleRepository.save(sale);
        });
    }
}
