package com.grecomilani.vehiclesales.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação OpenAPI, disponível em {@code /swagger-ui.html} e {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi(@Value("${api.version}") String version) {
        return new OpenAPI().info(new Info()
                .title("Vehicle Sales Service")
                .description("Serviço de venda de veículos: listagens de veículos à venda e vendidos, compra e status de pagamento.")
                .version(version));
    }
}
