package com.menujpa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI menuJpaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MenuJPA API")
                .description("Sistema de gestión gastronómica: menús, recetas, alimentos y personal de un restaurante.")
                .version("v1")
                .contact(new Contact().name("Martín Zamora")));
    }
}
