package com.grecomilani.vehiclesales.domain.vehicle;

import com.grecomilani.vehiclesales.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Year;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleTest {

    private static final UUID ID = UUID.randomUUID();
    private static final BigDecimal PRICE = new BigDecimal("85000.00");

    @Test
    void createShouldStartAvailableAndTrimTexts() {
        Vehicle vehicle = Vehicle.create(ID, " Toyota ", " Corolla ", 2022, " Prata ", PRICE);

        assertThat(vehicle.getId()).isEqualTo(ID);
        assertThat(vehicle.getBrand()).isEqualTo("Toyota");
        assertThat(vehicle.getModel()).isEqualTo("Corolla");
        assertThat(vehicle.getYear()).isEqualTo(2022);
        assertThat(vehicle.getColor()).isEqualTo("Prata");
        assertThat(vehicle.getPrice()).isEqualByComparingTo(PRICE);
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void shouldRequireIdAndStatus() {
        assertThatThrownBy(() -> Vehicle.create(null, "Toyota", "Corolla", 2022, "Prata", PRICE))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Vehicle(ID, "Toyota", "Corolla", 2022, "Prata", PRICE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldRejectBlankTexts(String blank) {
        assertThatThrownBy(() -> Vehicle.create(ID, blank, "Corolla", 2022, "Prata", PRICE))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("marca");
        assertThatThrownBy(() -> Vehicle.create(ID, "Toyota", blank, 2022, "Prata", PRICE))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("modelo");
        assertThatThrownBy(() -> Vehicle.create(ID, "Toyota", "Corolla", 2022, blank, PRICE))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("cor");
    }

    @Test
    void shouldAcceptYearWithinBounds() {
        int nextYear = Year.now().getValue() + 1;

        assertThat(Vehicle.create(ID, "Ford", "Model T", 1900, "Preto", PRICE).getYear()).isEqualTo(1900);
        assertThat(Vehicle.create(ID, "Toyota", "Corolla", nextYear, "Prata", PRICE).getYear()).isEqualTo(nextYear);
    }

    @Test
    void shouldRejectYearOutOfBounds() {
        int tooNew = Year.now().getValue() + 2;

        assertThatThrownBy(() -> Vehicle.create(ID, "Toyota", "Corolla", 1899, "Prata", PRICE))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ano");
        assertThatThrownBy(() -> Vehicle.create(ID, "Toyota", "Corolla", tooNew, "Prata", PRICE))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ano");
    }

    @Test
    void shouldRejectNonPositivePrice() {
        assertThatThrownBy(() -> Vehicle.create(ID, "Toyota", "Corolla", 2022, "Prata", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("preço");
        assertThatThrownBy(() -> Vehicle.create(ID, "Toyota", "Corolla", 2022, "Prata", BigDecimal.ZERO))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> Vehicle.create(ID, "Toyota", "Corolla", 2022, "Prata", new BigDecimal("-1")))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void updateDetailsShouldChangeDataOfAvailableVehicle() {
        Vehicle vehicle = Vehicle.create(ID, "Toyota", "Corolla", 2022, "Prata", PRICE);

        vehicle.updateDetails("Toyota", "Corolla Cross", 2023, "Branco", new BigDecimal("99000.00"));

        assertThat(vehicle.getModel()).isEqualTo("Corolla Cross");
        assertThat(vehicle.getYear()).isEqualTo(2023);
        assertThat(vehicle.getColor()).isEqualTo("Branco");
        assertThat(vehicle.getPrice()).isEqualByComparingTo("99000.00");
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESERVED", "SOLD"})
    void updateDetailsShouldBeRejectedWhenVehicleIsNotAvailable(VehicleStatus status) {
        Vehicle vehicle = new Vehicle(ID, "Toyota", "Corolla", 2022, "Prata", PRICE, status);

        assertThatThrownBy(() -> vehicle.updateDetails("Toyota", "Corolla", 2022, "Prata", BigDecimal.TEN))
                .isInstanceOf(VehicleNotAvailableException.class)
                .hasMessageContaining(ID.toString())
                .hasMessageContaining(status.name());
        assertThat(vehicle.getPrice()).isEqualByComparingTo(PRICE);
    }
}
