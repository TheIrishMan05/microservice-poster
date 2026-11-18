package ifmo.poster.event.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventServiceOpenApi(@Value("${openapi.server-url}") String serverUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Service API")
                        .description("События, места (seats), теги, регистрации, типы билетов и waitlist")
                        .version("1.0.0"))
                .servers(List.of(new Server()
                        .url(serverUrl)
                        .description("API Gateway")));
    }
}
