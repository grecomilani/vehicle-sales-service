package com.grecomilani.vehiclesales.infrastructure.web.sale;

import com.grecomilani.vehiclesales.application.usecase.PurchaseVehicleCommand;
import com.grecomilani.vehiclesales.application.usecase.PurchaseVehicleUseCase;
import com.grecomilani.vehiclesales.domain.sale.Cpf;
import com.grecomilani.vehiclesales.domain.sale.PaymentStatus;
import com.grecomilani.vehiclesales.domain.sale.Sale;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleNotAvailableException;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleNotFoundException;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaleController.class)
class SaleControllerTest {

    private static final UUID VEHICLE_ID = UUID.fromString("3f1c2a4e-8b7d-4c1a-9e2f-1a2b3c4d5e6f");
    private static final String VALID_BODY = """
            {"vehicleId": "3f1c2a4e-8b7d-4c1a-9e2f-1a2b3c4d5e6f",
             "buyerCpf": "529.982.247-25",
             "saleDate": "2026-10-01"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurchaseVehicleUseCase purchaseVehicleUseCase;

    @Test
    void shouldCreateSaleWithPendingPayment() throws Exception {
        UUID saleId = UUID.randomUUID();
        Sale sale = new Sale(saleId, VEHICLE_ID, new Cpf("52998224725"), LocalDate.of(2026, 10, 1),
                new BigDecimal("85000.00"), "pay-123", PaymentStatus.PENDING, 0L);
        when(purchaseVehicleUseCase.execute(any(PurchaseVehicleCommand.class))).thenReturn(sale);

        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saleId.toString()))
                .andExpect(jsonPath("$.vehicleId").value(VEHICLE_ID.toString()))
                .andExpect(jsonPath("$.buyerCpf").value("52998224725"))
                .andExpect(jsonPath("$.saleDate").value("2026-10-01"))
                .andExpect(jsonPath("$.price").value(85000.00))
                .andExpect(jsonPath("$.paymentCode").value("pay-123"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"));
    }

    @Test
    void shouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"buyerCpf": "111.111.111-11"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vehicleId").exists())
                .andExpect(jsonPath("$.errors.buyerCpf").exists())
                .andExpect(jsonPath("$.errors.saleDate").exists());

        verify(purchaseVehicleUseCase, never()).execute(any());
    }

    @Test
    void shouldReturnNotFoundWhenVehicleDoesNotExist() throws Exception {
        when(purchaseVehicleUseCase.execute(any())).thenThrow(new VehicleNotFoundException(VEHICLE_ID));

        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    void shouldReturnConflictWhenVehicleIsNotAvailable() throws Exception {
        when(purchaseVehicleUseCase.execute(any()))
                .thenThrow(new VehicleNotAvailableException(VEHICLE_ID, VehicleStatus.RESERVED));

        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Veículo indisponível"));
    }

    @Test
    void shouldReturnConflictOnConcurrentPurchase() throws Exception {
        when(purchaseVehicleUseCase.execute(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException("VehicleEntity", VEHICLE_ID));

        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de concorrência"));
    }

    @Test
    void shouldReturnConflictOnDatabaseConstraintViolation() throws Exception {
        when(purchaseVehicleUseCase.execute(any()))
                .thenThrow(new DataIntegrityViolationException("uq_sales_active_vehicle"));

        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de dados"));
    }
}
