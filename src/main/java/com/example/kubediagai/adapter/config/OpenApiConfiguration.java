package com.example.kubediagai.adapter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI kubeDiagAiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("kube-diag-ai API")
                        .description("Kubernetes diagnostic assistant API")
                        .version("0.0.1-SNAPSHOT"));
    }
}
