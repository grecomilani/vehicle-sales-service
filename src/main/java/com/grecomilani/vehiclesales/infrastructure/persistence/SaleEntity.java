package com.grecomilani.vehiclesales.infrastructure.persistence;

import com.grecomilani.vehiclesales.domain.sale.Cpf;
import com.grecomilani.vehiclesales.domain.sale.PaymentStatus;
import com.grecomilani.vehiclesales.domain.sale.Sale;
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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sales")
class SaleEntity {

    @Id
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "buyer_cpf", nullable = false, length = 11)
    private String buyerCpf;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "payment_code", nullable = false, unique = true, length = 36)
    private String paymentCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SaleEntity() {
        // exigido pelo JPA
    }

    static SaleEntity from(Sale sale) {
        SaleEntity entity = new SaleEntity();
        entity.id = sale.getId();
        entity.vehicleId = sale.getVehicleId();
        entity.buyerCpf = sale.getBuyerCpf().value();
        entity.saleDate = sale.getSaleDate();
        entity.price = sale.getPrice();
        entity.paymentCode = sale.getPaymentCode();
        entity.paymentStatus = sale.getPaymentStatus();
        entity.version = sale.getVersion();
        return entity;
    }

    Sale toDomain() {
        return new Sale(id, vehicleId, new Cpf(buyerCpf), saleDate, price, paymentCode, paymentStatus, version);
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
