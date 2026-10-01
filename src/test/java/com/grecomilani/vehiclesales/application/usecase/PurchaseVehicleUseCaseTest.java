package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.application.port.SaleRepository;
import com.grecomilani.vehiclesales.application.port.TransactionRunner;
import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.exception.DomainException;
import com.grecomilani.vehiclesales.domain.sale.PaymentStatus;
import com.grecomilani.vehiclesales.domain.sale.Sale;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleNotAvailableException;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleNotFoundException;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseVehicleUseCaseTest {

    private static final UUID VEHICLE_ID = UUID.randomUUID();
    private static final String CPF = "529.982.247-25";
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private SaleRepository saleRepository;

    private PurchaseVehicleUseCase useCase;

    @BeforeEach
    void setUp() {
        TransactionRunner directRunner = new TransactionRunner() {
            @Override
            public <T> T inTransaction(Supplier<T> operation) {
                return operation.get();
            }
        };
        Clock clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZONE);
        useCase = new PurchaseVehicleUseCase(vehicleRepository, saleRepository, directRunner, clock);
    }

    @Test
    void shouldReserveVehicleAndCreatePendingSale() {
        Vehicle vehicle = new Vehicle(VEHICLE_ID, "Toyota", "Corolla", 2022, "Prata",
                new BigDecimal("85000.00"), VehicleStatus.AVAILABLE, 0L);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle));
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Sale sale = useCase.execute(new PurchaseVehicleCommand(VEHICLE_ID, CPF, TODAY));

        ArgumentCaptor<Vehicle> savedVehicle = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(savedVehicle.capture());
        assertThat(savedVehicle.getValue().getStatus()).isEqualTo(VehicleStatus.RESERVED);
        assertThat(savedVehicle.getValue().getVersion()).isZero();

        assertThat(sale.getVehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(sale.getBuyerCpf().value()).isEqualTo("52998224725");
        assertThat(sale.getSaleDate()).isEqualTo(TODAY);
        assertThat(sale.getPrice()).isEqualByComparingTo("85000.00");
        assertThat(sale.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldFailWhenVehicleDoesNotExist() {
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new PurchaseVehicleCommand(VEHICLE_ID, CPF, TODAY)))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining(VEHICLE_ID.toString());
        verifyNoInteractions(saleRepository);
    }

    @Test
    void shouldFailWhenVehicleIsNotAvailable() {
        Vehicle sold = new Vehicle(VEHICLE_ID, "Toyota", "Corolla", 2022, "Prata",
                new BigDecimal("85000.00"), VehicleStatus.SOLD, 2L);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(sold));

        assertThatThrownBy(() -> useCase.execute(new PurchaseVehicleCommand(VEHICLE_ID, CPF, TODAY)))
                .isInstanceOf(VehicleNotAvailableException.class);
        verify(vehicleRepository, never()).save(any());
        verifyNoInteractions(saleRepository);
    }

    @Test
    void shouldFailWhenSaleDateIsInTheFutureUsingConfiguredTimeZone() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID, "Toyota", "Corolla", 2022, "Prata", new BigDecimal("85000.00"));
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> useCase.execute(new PurchaseVehicleCommand(VEHICLE_ID, CPF, TODAY.plusDays(1))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("futuro");
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void shouldValidateCpfBeforeTouchingRepositories() {
        assertThatThrownBy(() -> useCase.execute(new PurchaseVehicleCommand(VEHICLE_ID, "123", TODAY)))
                .isInstanceOf(DomainException.class)
                .hasMessage("CPF inválido.");
        verifyNoInteractions(vehicleRepository, saleRepository);
    }
}
