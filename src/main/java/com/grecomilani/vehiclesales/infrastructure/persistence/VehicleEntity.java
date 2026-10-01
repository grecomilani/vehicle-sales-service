package com.grecomilani.vehiclesales.infrastructure.persistence;

import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
class VehicleEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    private int year;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected VehicleEntity() {
        // exigido pelo JPA
    }

    /**
     * Converte o veículo do domínio, preservando a versão lida do banco: ao
     * salvar, o JPA compara essa versão com a atual e rejeita a alteração se
     * outra transação tiver modificado o veículo nesse meio-tempo.
     */
    static VehicleEntity from(Vehicle vehicle) {
        VehicleEntity entity = new VehicleEntity();
        entity.id = vehicle.getId();
        entity.brand = vehicle.getBrand();
        entity.model = vehicle.getModel();
        entity.year = vehicle.getYear();
        entity.color = vehicle.getColor();
        entity.price = vehicle.getPrice();
        entity.status = vehicle.getStatus();
        entity.version = vehicle.getVersion();
        return entity;
    }

    Vehicle toDomain() {
        return new Vehicle(id, brand, model, year, color, price, status, version);
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
