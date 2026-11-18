package ifmo.poster.order.config;

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
    public OpenAPI orderServiceOpenApi(@Value("${openapi.server-url}") String serverUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Заказы и билеты (оркестрация через Feign)")
                        .version("1.0.0"))
                .servers(List.of(new Server()
                        .url(serverUrl)
                        .description("API Gateway")));
    }
}
