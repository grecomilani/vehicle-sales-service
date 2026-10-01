package com.grecomilani.vehiclesales.infrastructure.web.vehicle;

import com.grecomilani.vehiclesales.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Listagem de veículos à venda contra um PostgreSQL real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class VehicleListingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.update("DELETE FROM vehicles");

        createVehicle("Civic", "120000.00", "AVAILABLE");
        createVehicle("Uno", "30000.00", "AVAILABLE");
        createVehicle("Onix", "70000.00", "AVAILABLE");
        createVehicle("Gol", "45000.00", "RESERVED");
        createVehicle("Corolla", "15000.00", "SOLD");
    }

    @Test
    void shouldListOnlyAvailableVehiclesOrderedByPriceAscending() throws Exception {
        mockMvc.perform(get("/vehicles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].model", contains("Uno", "Onix", "Civic")))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldPaginateResults() throws Exception {
        mockMvc.perform(get("/vehicles/available").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].model", contains("Civic")))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    private void createVehicle(String model, String price, String status) throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/internal/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"brand": "Marca", "model": "%s", "year": 2020, "color": "Preto", "price": %s}
                                """.formatted(model, price)))
                .andExpect(status().isCreated());
        jdbcTemplate.update("UPDATE vehicles SET status = ? WHERE id = ?", status, id);
    }
}
