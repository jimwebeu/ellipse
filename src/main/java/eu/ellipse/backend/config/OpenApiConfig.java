package eu.ellipse.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ellipseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ellipse Backend API")
                        .version("0.0.1-SNAPSHOT"));
    }
}
