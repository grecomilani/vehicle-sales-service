package com.grecomilani.vehiclesales.infrastructure.web.vehicle;

import com.grecomilani.vehiclesales.application.port.PageResult;
import com.grecomilani.vehiclesales.application.usecase.ListAvailableVehiclesUseCase;
import com.grecomilani.vehiclesales.application.usecase.ListSoldVehiclesUseCase;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListAvailableVehiclesUseCase listAvailableVehiclesUseCase;

    @MockitoBean
    private ListSoldVehiclesUseCase listSoldVehiclesUseCase;

    @Test
    void shouldListAvailableVehiclesWithDefaultPagination() throws Exception {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = Vehicle.create(id, "Fiat", "Uno", 2015, "Branco", new BigDecimal("30000.00"));
        when(listAvailableVehiclesUseCase.execute(0, 20))
                .thenReturn(new PageResult<>(List.of(vehicle), 0, 20, 1));

        mockMvc.perform(get("/vehicles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].brand").value("Fiat"))
                .andExpect(jsonPath("$.content[0].price").value(30000.00))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldForwardPaginationParameters() throws Exception {
        when(listAvailableVehiclesUseCase.execute(2, 5))
                .thenReturn(new PageResult<>(List.of(), 2, 5, 10));

        mockMvc.perform(get("/vehicles/available").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldRejectInvalidPaginationParameters() throws Exception {
        mockMvc.perform(get("/vehicles/available")
                        .param("page", "-1")
                        .param("size", String.valueOf(VehicleController.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.page").exists())
                .andExpect(jsonPath("$.errors.size").exists());

        verify(listAvailableVehiclesUseCase, never()).execute(anyInt(), anyInt());
    }

    @Test
    void shouldListSoldVehicles() throws Exception {
        UUID id = UUID.randomUUID();
        Vehicle sold = new Vehicle(id, "Fiat", "Uno", 2015, "Branco", new BigDecimal("30000.00"),
                VehicleStatus.SOLD, 2L);
        when(listSoldVehiclesUseCase.execute(1, 10)).thenReturn(new PageResult<>(List.of(sold), 1, 10, 11));

        mockMvc.perform(get("/vehicles/sold").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].status").value("SOLD"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldRejectInvalidPaginationParametersOnSoldListing() throws Exception {
        mockMvc.perform(get("/vehicles/sold").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.size").exists());

        verify(listSoldVehiclesUseCase, never()).execute(anyInt(), anyInt());
    }
}
