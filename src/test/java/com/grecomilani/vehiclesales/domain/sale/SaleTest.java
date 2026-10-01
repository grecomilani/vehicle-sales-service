package com.grecomilani.vehiclesales.domain.sale;

import com.grecomilani.vehiclesales.domain.exception.ConflictException;
import com.grecomilani.vehiclesales.domain.exception.DomainException;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaleTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);
    private static final Cpf CPF = new Cpf("52998224725");

    private final Vehicle vehicle = Vehicle.create(UUID.randomUUID(), "Toyota", "Corolla", 2022, "Prata",
            new BigDecimal("85000.00"));

    @Test
    void startShouldCreatePendingSaleWithVehiclePriceAndNewPaymentCode() {
        Sale sale = Sale.start(vehicle, CPF, TODAY, TODAY);

        assertThat(sale.getId()).isNotNull();
        assertThat(sale.getVehicleId()).isEqualTo(vehicle.getId());
        assertThat(sale.getBuyerCpf()).isEqualTo(CPF);
        assertThat(sale.getSaleDate()).isEqualTo(TODAY);
        assertThat(sale.getPrice()).isEqualByComparingTo("85000.00");
        assertThat(sale.getPaymentCode()).isNotBlank();
        assertThat(sale.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(sale.getVersion()).isNull();
    }

    @Test
    void eachSaleShouldReceiveDistinctIdentifiers() {
        Sale first = Sale.start(vehicle, CPF, TODAY, TODAY);
        Sale second = Sale.start(vehicle, CPF, TODAY, TODAY);

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(first.getPaymentCode()).isNotEqualTo(second.getPaymentCode());
    }

    @Test
    void startShouldAcceptPastDates() {
        assertThat(Sale.start(vehicle, CPF, TODAY.minusDays(3), TODAY).getSaleDate())
                .isEqualTo(TODAY.minusDays(3));
    }

    @Test
    void startShouldRejectFutureDate() {
        assertThatThrownBy(() -> Sale.start(vehicle, CPF, TODAY.plusDays(1), TODAY))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("futuro");
    }

    @Test
    void startShouldRequireSaleDate() {
        assertThatThrownBy(() -> Sale.start(vehicle, CPF, null, TODAY))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"PAID", "CANCELED"})
    void registerPaymentResultShouldFinalizePendingPayment(PaymentStatus result) {
        Sale sale = Sale.start(vehicle, CPF, TODAY, TODAY);

        assertThat(sale.registerPaymentResult(result)).isTrue();
        assertThat(sale.getPaymentStatus()).isEqualTo(result);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"PAID", "CANCELED"})
    void registerPaymentResultShouldBeIdempotent(PaymentStatus result) {
        Sale sale = saleWithStatus(result);

        assertThat(sale.registerPaymentResult(result)).isFalse();
        assertThat(sale.getPaymentStatus()).isEqualTo(result);
    }

    @Test
    void finalizedPaymentShouldNotChangeToAnotherResult() {
        Sale paid = saleWithStatus(PaymentStatus.PAID);
        Sale canceled = saleWithStatus(PaymentStatus.CANCELED);

        assertThatThrownBy(() -> paid.registerPaymentResult(PaymentStatus.CANCELED))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PAID");
        assertThatThrownBy(() -> canceled.registerPaymentResult(PaymentStatus.PAID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CANCELED");
        assertThat(paid.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(canceled.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = PaymentStatus.class, names = "PENDING")
    void registerPaymentResultShouldRejectNonFinalResult(PaymentStatus result) {
        Sale sale = Sale.start(vehicle, CPF, TODAY, TODAY);

        assertThatThrownBy(() -> sale.registerPaymentResult(result))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("PAID ou CANCELED");
        assertThat(sale.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    private Sale saleWithStatus(PaymentStatus status) {
        return new Sale(UUID.randomUUID(), vehicle.getId(), CPF, TODAY, BigDecimal.TEN, "code", status, 1L);
    }

    @Test
    void constructorShouldRequireMandatoryFields() {
        UUID id = UUID.randomUUID();
        UUID vehicleId = vehicle.getId();
        BigDecimal price = BigDecimal.TEN;

        assertThatThrownBy(() -> new Sale(null, vehicleId, CPF, TODAY, price, "code", PaymentStatus.PENDING, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Sale(id, null, CPF, TODAY, price, "code", PaymentStatus.PENDING, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Sale(id, vehicleId, null, TODAY, price, "code", PaymentStatus.PENDING, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Sale(id, vehicleId, CPF, null, price, "code", PaymentStatus.PENDING, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Sale(id, vehicleId, CPF, TODAY, null, "code", PaymentStatus.PENDING, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Sale(id, vehicleId, CPF, TODAY, price, null, PaymentStatus.PENDING, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Sale(id, vehicleId, CPF, TODAY, price, "code", null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
