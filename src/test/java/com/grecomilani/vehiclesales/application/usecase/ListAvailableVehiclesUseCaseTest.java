package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.application.port.PageResult;
import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAvailableVehiclesUseCaseTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Test
    void shouldListOnlyAvailableVehicles() {
        Vehicle vehicle = Vehicle.create(UUID.randomUUID(), "Fiat", "Uno", 2015, "Branco", new BigDecimal("30000"));
        PageResult<Vehicle> page = new PageResult<>(List.of(vehicle), 1, 10, 11);
        when(vehicleRepository.findByStatusOrderByPriceAsc(VehicleStatus.AVAILABLE, 1, 10)).thenReturn(page);

        PageResult<Vehicle> result = new ListAvailableVehiclesUseCase(vehicleRepository).execute(1, 10);

        assertThat(result).isSameAs(page);
    }
}
