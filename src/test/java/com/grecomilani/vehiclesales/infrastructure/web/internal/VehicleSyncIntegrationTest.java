package com.grecomilani.vehiclesales.infrastructure.web.internal;

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

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo de sincronização: HTTP -> caso de uso -> JPA -> PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class VehicleSyncIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM vehicles");
    }

    @Test
    void shouldCreateAndThenUpdateVehicle() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/internal/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Corolla", "85000.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        mockMvc.perform(put("/internal/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Corolla Cross", "99000.50")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Corolla Cross"));

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM vehicles WHERE id = ?", id);
        assertThat(row.get("model")).isEqualTo("Corolla Cross");
        assertThat((BigDecimal) row.get("price")).isEqualByComparingTo("99000.50");
        assertThat(row.get("status")).isEqualTo("AVAILABLE");
        assertThat(row.get("version")).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vehicles", Integer.class)).isEqualTo(1);
    }

    @Test
    void shouldRejectUpdateOfReservedVehicle() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/internal/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Corolla", "85000.00")))
                .andExpect(status().isCreated());
        jdbcTemplate.update("UPDATE vehicles SET status = 'RESERVED' WHERE id = ?", id);

        mockMvc.perform(put("/internal/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Corolla", "70000.00")))
                .andExpect(status().isConflict());

        BigDecimal price = jdbcTemplate.queryForObject("SELECT price FROM vehicles WHERE id = ?", BigDecimal.class, id);
        assertThat(price).isEqualByComparingTo("85000.00");
    }

    private static String body(String model, String price) {
        return """
                {"brand": "Toyota", "model": "%s", "year": 2022, "color": "Prata", "price": %s}
                """.formatted(model, price);
    }
}
