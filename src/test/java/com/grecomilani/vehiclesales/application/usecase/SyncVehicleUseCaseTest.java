package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleNotAvailableException;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncVehicleUseCaseTest {

    private static final UUID ID = UUID.randomUUID();

    @Mock
    private VehicleRepository vehicleRepository;

    private SyncVehicleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SyncVehicleUseCase(vehicleRepository);
    }

    @Test
    void shouldCreateVehicleWhenItDoesNotExist() {
        when(vehicleRepository.findById(ID)).thenReturn(Optional.empty());
        when(vehicleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SyncVehicleResult result = useCase.execute(command("Corolla", "85000.00"));

        assertThat(result.created()).isTrue();
        assertThat(result.vehicle().getId()).isEqualTo(ID);
        assertThat(result.vehicle().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(result.vehicle().getPrice()).isEqualByComparingTo("85000.00");
    }

    @Test
    void shouldUpdateVehicleWhenItAlreadyExists() {
        Vehicle existing = Vehicle.create(ID, "Toyota", "Corolla", 2022, "Prata", new BigDecimal("85000.00"));
        when(vehicleRepository.findById(ID)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SyncVehicleResult result = useCase.execute(command("Corolla Cross", "99000.00"));

        ArgumentCaptor<Vehicle> saved = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(saved.capture());
        assertThat(result.created()).isFalse();
        assertThat(saved.getValue().getModel()).isEqualTo("Corolla Cross");
        assertThat(saved.getValue().getPrice()).isEqualByComparingTo("99000.00");
    }

    @Test
    void shouldNotUpdateVehicleThatIsNotAvailable() {
        Vehicle reserved = new Vehicle(ID, "Toyota", "Corolla", 2022, "Prata",
                new BigDecimal("85000.00"), VehicleStatus.RESERVED, 1L);
        when(vehicleRepository.findById(ID)).thenReturn(Optional.of(reserved));

        assertThatThrownBy(() -> useCase.execute(command("Corolla", "70000.00")))
                .isInstanceOf(VehicleNotAvailableException.class);
        verify(vehicleRepository, never()).save(any());
    }

    private static SyncVehicleCommand command(String model, String price) {
        return new SyncVehicleCommand(ID, "Toyota", model, 2022, "Prata", new BigDecimal(price));
    }
}
