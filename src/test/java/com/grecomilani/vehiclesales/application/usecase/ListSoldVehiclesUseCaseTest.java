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
class ListSoldVehiclesUseCaseTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Test
    void shouldListOnlySoldVehicles() {
        Vehicle sold = new Vehicle(UUID.randomUUID(), "Fiat", "Uno", 2015, "Branco",
                new BigDecimal("30000"), VehicleStatus.SOLD, 2L);
        PageResult<Vehicle> page = new PageResult<>(List.of(sold), 0, 20, 1);
        when(vehicleRepository.findByStatusOrderByPriceAsc(VehicleStatus.SOLD, 0, 20)).thenReturn(page);

        PageResult<Vehicle> result = new ListSoldVehiclesUseCase(vehicleRepository).execute(0, 20);

        assertThat(result).isSameAs(page);
    }
}
