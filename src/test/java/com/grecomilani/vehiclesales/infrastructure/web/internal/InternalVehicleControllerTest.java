package com.grecomilani.vehiclesales.infrastructure.web.internal;

import com.grecomilani.vehiclesales.application.usecase.SyncVehicleCommand;
import com.grecomilani.vehiclesales.application.usecase.SyncVehicleResult;
import com.grecomilani.vehiclesales.application.usecase.SyncVehicleUseCase;
import com.grecomilani.vehiclesales.domain.exception.DomainException;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleNotAvailableException;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalVehicleController.class)
class InternalVehicleControllerTest {

    private static final UUID ID = UUID.fromString("3f1c2a4e-8b7d-4c1a-9e2f-1a2b3c4d5e6f");
    private static final String VALID_BODY = """
            {"brand": "Toyota", "model": "Corolla", "year": 2022, "color": "Prata", "price": 85000.00}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SyncVehicleUseCase syncVehicleUseCase;

    @Test
    void shouldReturnCreatedWhenVehicleIsNew() throws Exception {
        when(syncVehicleUseCase.execute(any(SyncVehicleCommand.class)))
                .thenReturn(new SyncVehicleResult(vehicle(), true));

        mockMvc.perform(put("/internal/vehicles/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.year").value(2022))
                .andExpect(jsonPath("$.color").value("Prata"))
                .andExpect(jsonPath("$.price").value(85000.00))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void shouldReturnOkWhenVehicleIsUpdated() throws Exception {
        when(syncVehicleUseCase.execute(any(SyncVehicleCommand.class)))
                .thenReturn(new SyncVehicleResult(vehicle(), false));

        mockMvc.perform(put("/internal/vehicles/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void shouldReturnBadRequestWithFieldErrorsWhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(put("/internal/vehicles/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"brand": "", "model": "Corolla", "year": 1800, "price": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.brand").exists())
                .andExpect(jsonPath("$.errors.year").exists())
                .andExpect(jsonPath("$.errors.color").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.model").doesNotExist());

        verify(syncVehicleUseCase, never()).execute(any());
    }

    @Test
    void shouldReturnBadRequestWhenIdIsNotUuid() throws Exception {
        mockMvc.perform(put("/internal/vehicles/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenVehicleIsNotAvailable() throws Exception {
        when(syncVehicleUseCase.execute(any(SyncVehicleCommand.class)))
                .thenThrow(new VehicleNotAvailableException(ID, VehicleStatus.SOLD));

        mockMvc.perform(put("/internal/vehicles/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Veículo indisponível"))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void shouldReturnUnprocessableContentWhenBusinessRuleIsViolated() throws Exception {
        when(syncVehicleUseCase.execute(any(SyncVehicleCommand.class)))
                .thenThrow(new DomainException("O ano deve estar entre 1900 e 2027."));

        mockMvc.perform(put("/internal/vehicles/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"))
                .andExpect(jsonPath("$.detail").value("O ano deve estar entre 1900 e 2027."));
    }

    private static Vehicle vehicle() {
        return Vehicle.create(ID, "Toyota", "Corolla", 2022, "Prata", new BigDecimal("85000.00"));
    }
}
