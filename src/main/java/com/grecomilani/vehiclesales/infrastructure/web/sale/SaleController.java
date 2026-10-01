package com.grecomilani.vehiclesales.infrastructure.web.sale;

import com.grecomilani.vehiclesales.application.usecase.PurchaseVehicleUseCase;
import com.grecomilani.vehiclesales.infrastructure.web.SaleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de venda de veículos, consumidos pelo frontend.
 */
@Tag(name = "Vendas", description = "Compra de veículos")
@RestController
@RequestMapping("/sales")
public class SaleController {

    private final PurchaseVehicleUseCase purchaseVehicleUseCase;

    public SaleController(PurchaseVehicleUseCase purchaseVehicleUseCase) {
        this.purchaseVehicleUseCase = purchaseVehicleUseCase;
    }

    /**
     * Efetua a compra de um veículo. O veículo fica reservado e a venda é criada
     * com o pagamento pendente; o {@code paymentCode} retornado é usado pela
     * entidade processadora de pagamentos para informar o resultado.
     */
    @Operation(summary = "Efetua a compra de um veículo (reserva o veículo e gera o código de pagamento)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse purchase(@Valid @RequestBody PurchaseRequest request) {
        return SaleResponse.from(purchaseVehicleUseCase.execute(request.toCommand()));
    }
}
