package com.grecomilani.vehiclesales.domain.sale;

import com.grecomilani.vehiclesales.domain.exception.DomainException;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Venda de um veículo. Nasce com o pagamento pendente e recebe um código de
 * pagamento, usado pela entidade processadora para informar o resultado.
 */
public class Sale {

    private final UUID id;
    private final UUID vehicleId;
    private final Cpf buyerCpf;
    private final LocalDate saleDate;
    private final BigDecimal price;
    private final String paymentCode;
    private final PaymentStatus paymentStatus;
    private final Long version;

    public Sale(UUID id, UUID vehicleId, Cpf buyerCpf, LocalDate saleDate, BigDecimal price,
                String paymentCode, PaymentStatus paymentStatus, Long version) {
        this.id = Objects.requireNonNull(id, "id é obrigatório");
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId é obrigatório");
        this.buyerCpf = Objects.requireNonNull(buyerCpf, "buyerCpf é obrigatório");
        this.saleDate = Objects.requireNonNull(saleDate, "saleDate é obrigatório");
        this.price = Objects.requireNonNull(price, "price é obrigatório");
        this.paymentCode = Objects.requireNonNull(paymentCode, "paymentCode é obrigatório");
        this.paymentStatus = Objects.requireNonNull(paymentStatus, "paymentStatus é obrigatório");
        this.version = version;
    }

    /**
     * Inicia a venda de um veículo já reservado, registrando o preço do momento
     * da compra e gerando um novo código de pagamento.
     *
     * @param today data atual, usada para impedir datas de venda no futuro
     */
    public static Sale start(Vehicle vehicle, Cpf buyerCpf, LocalDate saleDate, LocalDate today) {
        Objects.requireNonNull(saleDate, "saleDate é obrigatório");
        if (saleDate.isAfter(today)) {
            throw new DomainException("A data da venda não pode estar no futuro.");
        }
        return new Sale(UUID.randomUUID(), vehicle.getId(), buyerCpf, saleDate, vehicle.getPrice(),
                UUID.randomUUID().toString(), PaymentStatus.PENDING, null);
    }

    public UUID getId() {
        return id;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public Cpf getBuyerCpf() {
        return buyerCpf;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public Long getVersion() {
        return version;
    }
}
