package com.grecomilani.vehiclesales.domain.vehicle;

import com.grecomilani.vehiclesales.domain.exception.DomainException;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Objects;
import java.util.UUID;

/**
 * Veículo anunciado para venda.
 * <p>
 * O cadastro do veículo pertence ao {@code vehicle-platform-api}; este serviço
 * mantém uma cópia dos dados necessários para listagem e venda, identificada
 * pelo mesmo {@code id} do serviço principal.
 */
public class Vehicle {

    static final int MIN_YEAR = 1900;

    private final UUID id;
    private String brand;
    private String model;
    private int year;
    private String color;
    private BigDecimal price;
    private VehicleStatus status;
    private final Long version;

    /**
     * @param version versão para controle de concorrência otimista;
     *                {@code null} para um veículo ainda não persistido
     */
    public Vehicle(UUID id, String brand, String model, int year, String color,
                   BigDecimal price, VehicleStatus status, Long version) {
        this.id = Objects.requireNonNull(id, "id é obrigatório");
        this.status = Objects.requireNonNull(status, "status é obrigatório");
        this.version = version;
        applyDetails(brand, model, year, color, price);
    }

    /**
     * Cria um novo veículo, disponível para venda.
     */
    public static Vehicle create(UUID id, String brand, String model, int year,
                                 String color, BigDecimal price) {
        return new Vehicle(id, brand, model, year, color, price, VehicleStatus.AVAILABLE, null);
    }

    /**
     * Atualiza os dados cadastrais. Só é permitido enquanto o veículo estiver
     * disponível: um veículo reservado ou vendido não pode mudar de preço ou
     * de características.
     */
    public void updateDetails(String brand, String model, int year, String color, BigDecimal price) {
        requireAvailable();
        applyDetails(brand, model, year, color, price);
    }

    /**
     * Reserva o veículo para uma compra, enquanto o pagamento não é confirmado.
     */
    public void reserve() {
        requireAvailable();
        this.status = VehicleStatus.RESERVED;
    }

    private void requireAvailable() {
        if (status != VehicleStatus.AVAILABLE) {
            throw new VehicleNotAvailableException(id, status);
        }
    }

    private void applyDetails(String brand, String model, int year, String color, BigDecimal price) {
        this.brand = requireText(brand, "marca");
        this.model = requireText(model, "modelo");
        this.color = requireText(color, "cor");
        this.year = requireValidYear(year);
        this.price = requirePositive(price);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException("O campo %s é obrigatório.".formatted(field));
        }
        return value.strip();
    }

    private static int requireValidYear(int year) {
        int maxYear = Year.now().getValue() + 1;
        if (year < MIN_YEAR || year > maxYear) {
            throw new DomainException("O ano deve estar entre %d e %d.".formatted(MIN_YEAR, maxYear));
        }
        return year;
    }

    private static BigDecimal requirePositive(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new DomainException("O preço deve ser maior que zero.");
        }
        return price;
    }

    public UUID getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public String getColor() {
        return color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }
}
