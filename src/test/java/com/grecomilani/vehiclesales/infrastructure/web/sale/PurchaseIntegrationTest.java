package com.grecomilani.vehiclesales.infrastructure.web.sale;

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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo de compra contra um PostgreSQL real, incluindo compras simultâneas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PurchaseIntegrationTest {

    private static final String TODAY = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID vehicleId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.update("DELETE FROM sales");
        jdbcTemplate.update("DELETE FROM vehicles");

        vehicleId = UUID.randomUUID();
        mockMvc.perform(put("/internal/vehicles/{id}", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"brand": "Toyota", "model": "Corolla", "year": 2022, "color": "Prata", "price": 85000.00}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldPurchaseVehicleReservingItAndCreatingPendingSale() throws Exception {
        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(purchaseBody(vehicleId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicleId").value(vehicleId.toString()))
                .andExpect(jsonPath("$.buyerCpf").value("52998224725"))
                .andExpect(jsonPath("$.price").value(85000.00))
                .andExpect(jsonPath("$.paymentCode").isNotEmpty())
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"));

        assertThat(jdbcTemplate.queryForObject("SELECT status FROM vehicles WHERE id = ?", String.class, vehicleId))
                .isEqualTo("RESERVED");
        Map<String, Object> sale = jdbcTemplate.queryForMap("SELECT * FROM sales WHERE vehicle_id = ?", vehicleId);
        assertThat(sale.get("payment_status")).isEqualTo("PENDING");
        assertThat(sale.get("buyer_cpf")).isEqualTo("52998224725");

        mockMvc.perform(get("/vehicles/available"))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(vehicleId.toString()))));
    }

    @Test
    void shouldRejectSecondPurchaseOfSameVehicle() throws Exception {
        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(purchaseBody(vehicleId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(purchaseBody(vehicleId)))
                .andExpect(status().isConflict());

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sales", Integer.class)).isEqualTo(1);
    }

    @Test
    void shouldReturnNotFoundForUnknownVehicle() throws Exception {
        mockMvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseBody(UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    void concurrentPurchasesShouldResultInExactlyOneSale() throws Exception {
        int buyers = 8;
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Integer>> attempts = new ArrayList<>();
        for (int i = 0; i < buyers; i++) {
            attempts.add(() -> {
                start.await();
                return mockMvc.perform(post("/sales")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(purchaseBody(vehicleId)))
                        .andReturn().getResponse().getStatus();
            });
        }

        List<Integer> statuses = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(buyers)) {
            List<Future<Integer>> futures = attempts.stream().map(executor::submit).toList();
            start.countDown();
            for (Future<Integer> future : futures) {
                statuses.add(future.get());
            }
        }

        assertThat(statuses).containsOnly(201, 409);
        assertThat(statuses).filteredOn(code -> code == 201).hasSize(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sales", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM vehicles WHERE id = ?", String.class, vehicleId))
                .isEqualTo("RESERVED");
    }

    private static String purchaseBody(UUID vehicleId) {
        return """
                {"vehicleId": "%s", "buyerCpf": "529.982.247-25", "saleDate": "%s"}
                """.formatted(vehicleId, TODAY);
    }
}
