package dk.bko.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI documentation for the application.
     *
     * @return the OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JDBC Message Store API")
                        .description("Spring Boot REST API for managing messages in a JDBC store")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("JDBC Message Store Team")
                                .email("info@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}