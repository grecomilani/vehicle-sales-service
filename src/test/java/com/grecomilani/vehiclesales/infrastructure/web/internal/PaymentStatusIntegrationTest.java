package com.grecomilani.vehiclesales.infrastructure.web.internal;

import com.grecomilani.vehiclesales.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo de pagamento: compra -> resultado do pagamento -> estado do veículo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PaymentStatusIntegrationTest {

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
    void paidPaymentShouldMarkVehicleAsSoldAndBeIdempotent() throws Exception {
        String paymentCode = purchase();

        updatePayment(paymentCode, "PAID")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        assertThat(vehicleStatus()).isEqualTo("SOLD");
        assertThat(saleColumn(paymentCode, "payment_status")).isEqualTo("PAID");
        assertThat(saleColumn(paymentCode, "version")).isEqualTo(1L);

        // reenvio do mesmo resultado (webhook duplicado) não altera nada
        updatePayment(paymentCode, "PAID").andExpect(status().isOk());
        assertThat(saleColumn(paymentCode, "version")).isEqualTo(1L);

        // pagamento já efetuado não pode ser cancelado
        updatePayment(paymentCode, "CANCELED").andExpect(status().isConflict());
        assertThat(vehicleStatus()).isEqualTo("SOLD");
    }

    @Test
    void canceledPaymentShouldReleaseVehicleForANewPurchase() throws Exception {
        String paymentCode = purchase();

        updatePayment(paymentCode, "CANCELED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("CANCELED"));

        assertThat(vehicleStatus()).isEqualTo("AVAILABLE");
        mockMvc.perform(get("/vehicles/available"))
                .andExpect(jsonPath("$.content[*].id", hasItem(vehicleId.toString())));

        // o veículo pode ser comprado de novo; a venda cancelada não bloqueia
        String newPaymentCode = purchase();
        assertThat(newPaymentCode).isNotEqualTo(paymentCode);
        assertThat(vehicleStatus()).isEqualTo("RESERVED");
    }

    @Test
    void unknownPaymentCodeShouldReturnNotFound() throws Exception {
        updatePayment("codigo-inexistente", "PAID").andExpect(status().isNotFound());
    }

    private String purchase() throws Exception {
        String response = mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId": "%s", "buyerCpf": "529.982.247-25", "saleDate": "%s"}
                                """.formatted(vehicleId, TODAY)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.paymentCode");
    }

    private ResultActions updatePayment(String paymentCode, String status) throws Exception {
        return mockMvc.perform(patch("/internal/sales/{paymentCode}/payment", paymentCode)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status": "%s"}
                        """.formatted(status)));
    }

    private String vehicleStatus() {
        return jdbcTemplate.queryForObject("SELECT status FROM vehicles WHERE id = ?", String.class, vehicleId);
    }

    private Object saleColumn(String paymentCode, String column) {
        return jdbcTemplate.queryForMap("SELECT " + column + " FROM sales WHERE payment_code = ?", paymentCode)
                .get(column);
    }
}
