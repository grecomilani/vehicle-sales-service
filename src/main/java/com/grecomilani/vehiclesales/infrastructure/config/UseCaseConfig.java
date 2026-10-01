package com.grecomilani.vehiclesales.infrastructure.config;

import com.grecomilani.vehiclesales.application.port.SaleRepository;
import com.grecomilani.vehiclesales.application.port.TransactionRunner;
import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.application.usecase.ListAvailableVehiclesUseCase;
import com.grecomilani.vehiclesales.application.usecase.ListSoldVehiclesUseCase;
import com.grecomilani.vehiclesales.application.usecase.PurchaseVehicleUseCase;
import com.grecomilani.vehiclesales.application.usecase.SyncVehicleUseCase;
import com.grecomilani.vehiclesales.application.usecase.UpdatePaymentStatusUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Registra os casos de uso como beans, mantendo a camada de aplicação
 * livre de dependências do Spring.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    SyncVehicleUseCase syncVehicleUseCase(VehicleRepository vehicleRepository) {
        return new SyncVehicleUseCase(vehicleRepository);
    }

    @Bean
    ListAvailableVehiclesUseCase listAvailableVehiclesUseCase(VehicleRepository vehicleRepository) {
        return new ListAvailableVehiclesUseCase(vehicleRepository);
    }

    @Bean
    ListSoldVehiclesUseCase listSoldVehiclesUseCase(VehicleRepository vehicleRepository) {
        return new ListSoldVehiclesUseCase(vehicleRepository);
    }

    @Bean
    PurchaseVehicleUseCase purchaseVehicleUseCase(VehicleRepository vehicleRepository,
                                                  SaleRepository saleRepository,
                                                  TransactionRunner transactionRunner,
                                                  Clock clock) {
        return new PurchaseVehicleUseCase(vehicleRepository, saleRepository, transactionRunner, clock);
    }

    @Bean
    UpdatePaymentStatusUseCase updatePaymentStatusUseCase(SaleRepository saleRepository,
                                                          VehicleRepository vehicleRepository,
                                                          TransactionRunner transactionRunner) {
        return new UpdatePaymentStatusUseCase(saleRepository, vehicleRepository, transactionRunner);
    }

    /**
     * Relógio no fuso da operação da revenda, usado nas regras que dependem da
     * data atual (ex.: data da venda não pode estar no futuro).
     */
    @Bean
    Clock clock(@Value("${app.time-zone}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }
}
