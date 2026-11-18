package ifmo.poster.order.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Reactive Feign (Playtika) требует {@link WebClient.Builder}.
 * В test-среде Boot 4 иногда не поднимает этот бин автоматически.
 */
@Configuration
public class WebClientConfig {

    @Bean
    @ConditionalOnMissingBean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
