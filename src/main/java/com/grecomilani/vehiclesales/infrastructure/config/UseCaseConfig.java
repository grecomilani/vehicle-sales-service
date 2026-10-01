package com.grecomilani.vehiclesales.infrastructure.config;

import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.application.usecase.SyncVehicleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
