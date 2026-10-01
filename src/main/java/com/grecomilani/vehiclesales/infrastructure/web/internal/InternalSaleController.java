package com.grecomilani.vehiclesales.infrastructure.web.internal;

import com.grecomilani.vehiclesales.application.usecase.UpdatePaymentStatusCommand;
import com.grecomilani.vehiclesales.application.usecase.UpdatePaymentStatusUseCase;
import com.grecomilani.vehiclesales.infrastructure.web.SaleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints internos de vendas, chamados apenas pelo {@code vehicle-platform-api}.
 */
@Tag(name = "Interno", description = "Endpoints de uso exclusivo do vehicle-platform-api")
@RestController
@RequestMapping("/internal/sales")
public class InternalSaleController {

    private final UpdatePaymentStatusUseCase updatePaymentStatusUseCase;

    public InternalSaleController(UpdatePaymentStatusUseCase updatePaymentStatusUseCase) {
        this.updatePaymentStatusUseCase = updatePaymentStatusUseCase;
    }

    /**
     * Registra o resultado do pagamento (PAID ou CANCELED) da venda identificada
     * pelo código de pagamento. Reenvios do mesmo resultado são aceitos sem efeito.
     */
    @Operation(summary = "Registra o resultado do pagamento de uma venda")
    @PatchMapping("/{paymentCode}/payment")
    public SaleResponse updatePayment(@PathVariable String paymentCode,
                                      @Valid @RequestBody PaymentResultRequest request) {
        UpdatePaymentStatusCommand command =
                new UpdatePaymentStatusCommand(paymentCode, request.status().toPaymentStatus());
        return SaleResponse.from(updatePaymentStatusUseCase.execute(command));
    }
}
