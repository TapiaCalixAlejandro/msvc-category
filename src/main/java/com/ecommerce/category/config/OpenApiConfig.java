package com.ecommerce.category.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI categoryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservico de Categorias - Ecommerce API")
                        .description("API REST para la gestion de categorias de productos.")
                        .version("1.0"));
    }
}
