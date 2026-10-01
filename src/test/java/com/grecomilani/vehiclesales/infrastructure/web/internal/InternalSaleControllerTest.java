package com.grecomilani.vehiclesales.infrastructure.web.internal;

import com.grecomilani.vehiclesales.application.usecase.UpdatePaymentStatusCommand;
import com.grecomilani.vehiclesales.application.usecase.UpdatePaymentStatusUseCase;
import com.grecomilani.vehiclesales.domain.exception.ConflictException;
import com.grecomilani.vehiclesales.domain.sale.Cpf;
import com.grecomilani.vehiclesales.domain.sale.PaymentStatus;
import com.grecomilani.vehiclesales.domain.sale.Sale;
import com.grecomilani.vehiclesales.domain.sale.SaleNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalSaleController.class)
class InternalSaleControllerTest {

    private static final String PAYMENT_CODE = "pay-123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UpdatePaymentStatusUseCase updatePaymentStatusUseCase;

    @Test
    void shouldRegisterPaymentResult() throws Exception {
        when(updatePaymentStatusUseCase.execute(any())).thenReturn(sale(PaymentStatus.PAID));

        mockMvc.perform(patch("/internal/sales/{paymentCode}/payment", PAYMENT_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PAID"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentCode").value(PAYMENT_CODE))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        ArgumentCaptor<UpdatePaymentStatusCommand> command = ArgumentCaptor.forClass(UpdatePaymentStatusCommand.class);
        verify(updatePaymentStatusUseCase).execute(command.capture());
        assertThat(command.getValue().paymentCode()).isEqualTo(PAYMENT_CODE);
        assertThat(command.getValue().result()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void shouldMapCanceledResult() throws Exception {
        when(updatePaymentStatusUseCase.execute(any())).thenReturn(sale(PaymentStatus.CANCELED));

        mockMvc.perform(patch("/internal/sales/{paymentCode}/payment", PAYMENT_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CANCELED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("CANCELED"));
    }

    @Test
    void shouldRequireStatus() throws Exception {
        mockMvc.perform(patch("/internal/sales/{paymentCode}/payment", PAYMENT_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());

        verify(updatePaymentStatusUseCase, never()).execute(any());
    }

    @Test
    void shouldRejectStatusOtherThanPaidOrCanceled() throws Exception {
        mockMvc.perform(patch("/internal/sales/{paymentCode}/payment", PAYMENT_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PENDING"}
                                """))
                .andExpect(status().isBadRequest());

        verify(updatePaymentStatusUseCase, never()).execute(any());
    }

    @Test
    void shouldReturnNotFoundForUnknownPaymentCode() throws Exception {
        when(updatePaymentStatusUseCase.execute(any())).thenThrow(new SaleNotFoundException(PAYMENT_CODE));

        mockMvc.perform(patch("/internal/sales/{paymentCode}/payment", PAYMENT_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PAID"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenPaymentWasAlreadyFinalized() throws Exception {
        when(updatePaymentStatusUseCase.execute(any()))
                .thenThrow(new ConflictException("O pagamento da venda já foi finalizado como PAID."));

        mockMvc.perform(patch("/internal/sales/{paymentCode}/payment", PAYMENT_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CANCELED"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito"));
    }

    private static Sale sale(PaymentStatus status) {
        return new Sale(UUID.randomUUID(), UUID.randomUUID(), new Cpf("52998224725"), LocalDate.of(2026, 10, 1),
                new BigDecimal("85000.00"), PAYMENT_CODE, status, 1L);
    }
}
