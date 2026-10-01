package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.application.port.SaleRepository;
import com.grecomilani.vehiclesales.application.port.TransactionRunner;
import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.sale.Cpf;
import com.grecomilani.vehiclesales.domain.sale.PaymentStatus;
import com.grecomilani.vehiclesales.domain.sale.Sale;
import com.grecomilani.vehiclesales.domain.sale.SaleNotFoundException;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleNotFoundException;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class UpdatePaymentStatusUseCaseTest {

    private static final String PAYMENT_CODE = "pay-123";
    private static final UUID VEHICLE_ID = UUID.randomUUID();

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private UpdatePaymentStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        TransactionRunner directRunner = new TransactionRunner() {
            @Override
            public <T> T inTransaction(Supplier<T> operation) {
                return operation.get();
            }
        };
        useCase = new UpdatePaymentStatusUseCase(saleRepository, vehicleRepository, directRunner);
    }

    @Test
    void paidShouldConcludeSaleAndMarkVehicleAsSold() {
        givenPendingSaleAndReservedVehicle();

        Sale result = useCase.execute(new UpdatePaymentStatusCommand(PAYMENT_CODE, PaymentStatus.PAID));

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(savedVehicle().getStatus()).isEqualTo(VehicleStatus.SOLD);
    }

    @Test
    void canceledShouldCancelSaleAndReleaseVehicle() {
        givenPendingSaleAndReservedVehicle();

        Sale result = useCase.execute(new UpdatePaymentStatusCommand(PAYMENT_CODE, PaymentStatus.CANCELED));

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(savedVehicle().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void repeatedResultShouldNotChangeAnything() {
        when(saleRepository.findByPaymentCode(PAYMENT_CODE)).thenReturn(Optional.of(sale(PaymentStatus.PAID)));

        Sale result = useCase.execute(new UpdatePaymentStatusCommand(PAYMENT_CODE, PaymentStatus.PAID));

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verifyNoInteractions(vehicleRepository);
        verify(saleRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenPaymentCodeIsUnknown() {
        when(saleRepository.findByPaymentCode(PAYMENT_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdatePaymentStatusCommand(PAYMENT_CODE, PaymentStatus.PAID)))
                .isInstanceOf(SaleNotFoundException.class)
                .hasMessageContaining(PAYMENT_CODE);
        verifyNoInteractions(vehicleRepository);
    }

    @Test
    void shouldFailWhenVehicleOfSaleDoesNotExist() {
        when(saleRepository.findByPaymentCode(PAYMENT_CODE)).thenReturn(Optional.of(sale(PaymentStatus.PENDING)));
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdatePaymentStatusCommand(PAYMENT_CODE, PaymentStatus.PAID)))
                .isInstanceOf(VehicleNotFoundException.class);
        verify(saleRepository, never()).save(any());
    }

    private void givenPendingSaleAndReservedVehicle() {
        Vehicle reserved = new Vehicle(VEHICLE_ID, "Toyota", "Corolla", 2022, "Prata",
                new BigDecimal("85000.00"), VehicleStatus.RESERVED, 1L);
        when(saleRepository.findByPaymentCode(PAYMENT_CODE)).thenReturn(Optional.of(sale(PaymentStatus.PENDING)));
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(reserved));
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Vehicle savedVehicle() {
        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        return captor.getValue();
    }

    private static Sale sale(PaymentStatus status) {
        return new Sale(UUID.randomUUID(), VEHICLE_ID, new Cpf("52998224725"), LocalDate.of(2026, 10, 1),
                new BigDecimal("85000.00"), PAYMENT_CODE, status, 0L);
    }
}
